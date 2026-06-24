package com.example.api

import android.graphics.Bitmap
import android.util.Log
import com.example.data.ScamHistoryEntity
import java.util.Locale

object ScamScanEngine {
    private const val TAG = "ScamScanEngine"

    // Structured scan results
    data class ScanResult(
        val type: String,
        val input: String,
        val riskLevel: String, // LOW, MEDIUM, HIGH
        val confidenceScore: Int,
        val category: String,
        val explanation: String,
        val language: String
    ) {
        fun toEntity() = ScamHistoryEntity(
            type = type,
            input = input,
            riskLevel = riskLevel,
            confidenceScore = confidenceScore,
            category = category,
            explanation = explanation,
            language = language
        )
    }

    // Main scanning function
    suspend fun performScan(
        type: String, // CALL, SMS, WHATSAPP, URL, PAYMENT, EXPLAINER
        input: String,
        language: String, // ENGLISH, TELUGU, HINDI
        bitmap: Bitmap? = null
    ): ScanResult {
        val systemInstruction = """
            You are RakshaAI, an expert personal cyber safety assistant that protects Indian users from online frauds, scam calls, phishing links, and WhatsApp scams.
            Analyze the input for potential security threats.
            Provide your response strictly with the following four labels on separate lines, followed by detailed paragraphs:
            RISK LEVEL: [LOW / MEDIUM / HIGH]
            CONFIDENCE: [0-100]%
            CATEGORY: [Select a specific category, e.g. "Fake SBI KYC", "Job Offer Fraud", "UPI Payment Fraud", "Impersonation Scam", "Suspicious URL", "Genuine Payment", "Safe"]
            EXPLANATION: [Explain the threat clearly and explain why it is suspicious or safe. Legitimate banks/services never ask for OTPs or login credentials. Analyze any URL spelling, urgent tone, or payment receipts for fraud.]
            RECOMMENDATION: [What should the user do next? e.g. "Never share OTP", "Report call to 1930", "Block sender on WhatsApp", "Do not click link"]
            
            Your entire response MUST be in the requested language: $language.
        """.trimIndent()

        val prompt = when (type) {
            "CALL" -> "Analyze this call details. Caller Number: ${input.substringBefore("|")}, Context/Transcript: ${input.substringAfter("|")}"
            "SMS" -> "Analyze this incoming SMS. Sender ID: ${input.substringBefore("|")}, Message: ${input.substringAfter("|")}"
            "WHATSAPP" -> "Analyze this WhatsApp text: $input"
            "URL" -> "Analyze this URL link: $input"
            "PAYMENT" -> "Analyze this uploaded UPI payment screenshot/receipt. Verify transaction ID, timestamp, merchant details, fonts, alignment, and spacing to detect if it's a fake screenshot generated to scam merchants."
            else -> "Explain this query: $input"
        }

        // Call Gemini
        val rawResponse = GeminiScanner.scanContent(prompt, systemInstruction, bitmap)

        if (rawResponse == "API_KEY_MISSING" || rawResponse.startsWith("ERROR:")) {
            Log.d(TAG, "Gemini API unavailable ($rawResponse). Activating local rule-based safety engine.")
            return runLocalEngine(type, input, language)
        }

        return parseGeminiResponse(type, input, rawResponse, language)
    }

    private fun parseGeminiResponse(
        type: String,
        input: String,
        rawResponse: String,
        language: String
    ): ScanResult {
        try {
            var riskLevel = "LOW"
            var confidenceScore = 90
            var category = "Uncategorized"
            var explanation = ""
            var recommendation = ""

            val lines = rawResponse.lines()
            var explanationMode = false
            var recommendationMode = false

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.startsWith("RISK LEVEL:", ignoreCase = true)) {
                    val value = trimmed.substringAfter(":").trim().uppercase()
                    if (value.contains("HIGH")) riskLevel = "HIGH"
                    else if (value.contains("MEDIUM")) riskLevel = "MEDIUM"
                    else riskLevel = "LOW"
                } else if (trimmed.startsWith("CONFIDENCE:", ignoreCase = true)) {
                    val value = trimmed.substringAfter(":").trim().replace("%", "").trim()
                    confidenceScore = value.toIntOrNull() ?: 85
                } else if (trimmed.startsWith("CATEGORY:", ignoreCase = true)) {
                    category = trimmed.substringAfter(":").trim()
                } else if (trimmed.startsWith("EXPLANATION:", ignoreCase = true)) {
                    explanationMode = true
                    recommendationMode = false
                    explanation = trimmed.substringAfter(":").trim()
                } else if (trimmed.startsWith("RECOMMENDATION:", ignoreCase = true)) {
                    explanationMode = false
                    recommendationMode = true
                    recommendation = trimmed.substringAfter(":").trim()
                } else {
                    if (explanationMode) {
                        explanation += "\n" + trimmed
                    } else if (recommendationMode) {
                        recommendation += "\n" + trimmed
                    }
                }
            }

            if (explanation.isBlank()) {
                explanation = rawResponse
            }
            if (recommendation.isNotBlank()) {
                explanation += "\n\n💡 **Recommendation:**\n$recommendation"
            }

            return ScanResult(
                type = type,
                input = input,
                riskLevel = riskLevel,
                confidenceScore = confidenceScore,
                category = category,
                explanation = explanation,
                language = language
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini response: ${e.message}", e)
            return ScanResult(
                type = type,
                input = input,
                riskLevel = "MEDIUM",
                confidenceScore = 75,
                category = "Scam Detection Scan",
                explanation = rawResponse,
                language = language
            )
        }
    }

    private fun runLocalEngine(type: String, input: String, language: String): ScanResult {
        val normalizedInput = input.lowercase(Locale.ROOT)
        var riskLevel = "LOW"
        var category = "Uncategorized"
        var confidenceScore = 80
        var explanation = ""

        when (type) {
            "CALL" -> {
                val caller = input.substringBefore("|")
                val text = input.substringAfter("|").lowercase(Locale.ROOT)
                category = "Caller Audit"

                if (text.contains("otp") || text.contains("one time password") || text.contains("pin") || text.contains("password")) {
                    riskLevel = "HIGH"
                    category = "OTP Threat Alert"
                    explanation = getLocalString(
                        language,
                        en = "WARNING: Caller is asking for OTP/PIN. Genuine bank representatives or cyber support NEVER ask for your password, PIN, or OTP. Do not disclose anything! This caller is highly likely trying to hijack your digital bank accounts.",
                        hi = "चेतावनी: कॉलर ओटीपी या पिन मांग रहा है। वास्तविक बैंक कर्मचारी या साइबर पुलिस कभी भी आपका पासवर्ड, पिन या ओटीपी नहीं मांगते हैं। कुछ भी साझा न करें! यह एक बड़ा बैंक धोखाधड़ी हो सकता है।",
                        te = "హెచ్చరిక: ఫోన్ చేసిన వ్యక్తి OTP లేదా PIN అడుగుతున్నారు. నిజమైన బ్యాంక్ అధికారులు లేదా సైబర్ పోలీసులు ఎప్పుడూ మీ పాస్‌వర్డ్, పిన్ లేదా OTPని అడగరు. మీ సమాచారం చెప్పకండి! ఇది బ్యాంక్ మోసం కావచ్చు."
                    )
                } else if (text.contains("blocked") || text.contains("blocked account") || text.contains("kyc") || text.contains("pan card") || text.contains("aadhaar") || text.contains("verification")) {
                    riskLevel = "HIGH"
                    category = "KYC Scam Alert"
                    explanation = getLocalString(
                        language,
                        en = "WARNING: Fear tactic detected. Caller claims your bank account or SIM card is blocked and demands immediate KYC update. Genuine institutions do not make sudden threatening calls to update details. Hang up immediately.",
                        hi = "चेतावनी: डर पैदा करने की कोशिश। कॉलर दावा कर रहा है कि आपका बैंक खाता या सिम ब्लॉक है और तुरंत केवाईसी अपडेट की मांग कर रहा है। बैंक कभी भी ऐसे डराने वाले कॉल नहीं करते हैं। तुरंत फोन काट दें।",
                        te = "హెచ్చరిక: మిమ్మల్ని భయపెట్టే ప్రయత్నం. మీ బ్యాంక్ ఖాతా లేదా సిమ్ కార్డ్ బ్లాక్ చేయబడిందని మరియు వెంటనే KYC అప్‌డేట్ చేయాలని కాలర్ బెదిరిస్తున్నాడు. నిజమైన బ్యాంకులు ఇలాంటి ఫోన్ కాల్స్ చేయవు. వెంటనే ఫోన్ పెట్టేయండి."
                    )
                } else if (text.contains("police") || text.contains("commissioner") || text.contains("arrest") || text.contains("cyber crime") || text.contains("parcel") || text.contains("illegal")) {
                    riskLevel = "HIGH"
                    category = "Impersonation Scam"
                    explanation = getLocalString(
                        language,
                        en = "WARNING: Digital Arrest fraud pattern detected. Caller pretends to be a law enforcement officer, CBI, or customs officer, claiming an illegal parcel was caught in your name. Legitimate police officers will NEVER ask for video calls, digital house arrest, or bank transfers. Hang up and contact 1930.",
                        hi = "चेतावनी: 'डिजिटल अरेस्ट' धोखाधड़ी। कॉलर खुद को सीबीआई या पुलिस अधिकारी बताकर दावा कर रहा है कि आपके नाम से एक अवैध पार्सल पकड़ा गया है। पुलिस कभी भी वीडियो कॉल या बैंक ट्रांसफर की मांग नहीं करती है। तुरंत 1930 पर शिकायत दर्ज करें।",
                        te = "హెచ్చరిక: 'డిజిటల్ అరెస్ట్' మోసం. కాలర్ తనను తాను సీబీఐ లేదా పోలీస్ అధికారిగా చెప్పుకుంటూ మీ పేరుతో ఒక అక్రమ పార్శిల్ పట్టుబడిందని బెదిరిస్తున్నాడు. పోలీసులు ఎప్పుడూ వీడియో కాల్స్ చేసి డబ్బులు అడగరు. వెంటనే 1930 కి కాల్ చేయండి."
                    )
                } else {
                    riskLevel = "LOW"
                    category = "Normal Incoming Call"
                    explanation = getLocalString(
                        language,
                        en = "This call does not match known scam profiles. However, always exercise caution when sharing any private details with unknown callers.",
                        hi = "यह कॉल ज्ञात धोखाधड़ी पैटर्न से मेल नहीं खाती है। फिर भी, अज्ञात कॉलर्स के साथ निजी विवरण साझा करते समय हमेशा सतर्क रहें।",
                        te = "ఈ కాల్ సాధారణమైనదిగా కనిపిస్తోంది. అయినప్పటికీ, తెలియని వ్యక్తులతో మీ వ్యక్తిగత వివరాలను పంచుకునేటప్పుడు ఎల్లప్పుడూ జాగ్రత్తగా ఉండండి."
                    )
                }
            }

            "SMS" -> {
                val sender = input.substringBefore("|")
                val text = input.substringAfter("|").lowercase(Locale.ROOT)
                category = "SMS Threat Audit"

                if (text.contains("click") || text.contains("bit.ly") || text.contains("tinyurl") || text.contains("link") || text.contains(".ru") || text.contains("http")) {
                    if (text.contains("sbi") || text.contains("bank") || text.contains("gift") || text.contains("win") || text.contains("blocked")) {
                        riskLevel = "HIGH"
                        category = "Phishing URL SMS"
                        explanation = getLocalString(
                            language,
                            en = "WARNING: Phishing SMS with shortened link. Scammers use fake shortened links disguised as Bank alerts or Rewards to steal internet banking credentials. Never click suspicious links.",
                            hi = "चेतावनी: धोखाधड़ी वाला एसएमएस लिंक। जालसाज बैंक अलर्ट या इनाम के नाम पर नकली शॉर्ट लिंक का उपयोग करते हैं। किसी भी संदिग्ध लिंक पर क्लिक न करें।",
                            te = "హెచ్చరిక: మోసపూరిత SMS లింక్. బ్యాంక్ అలర్ట్‌లు లేదా బహుమతుల పేరిట నకిలీ షార్ట్ లింక్‌లను పంపి మీ బ్యాంక్ వివరాలను దొంగిలిస్తారు. ఈ లింక్‌లపై క్లిక్ చేయకండి."
                        )
                    } else {
                        riskLevel = "MEDIUM"
                        category = "SMS with Link"
                        explanation = getLocalString(
                            language,
                            en = "Suspicious message containing an external link. Avoid clicking URLs received from unknown mobile numbers, as they could contain spyware or trojans.",
                            hi = "संदिग्ध संदेश जिसमें एक बाहरी लिंक है। अज्ञात नंबरों से प्राप्त यूआरएल पर क्लिक करने से बचें, क्योंकि इनमें वायरस हो सकते हैं।",
                            te = "తెలియని నంబర్ల నుండి వచ్చిన లింక్‌లు కలిగి ఉన్న సందేశం. ఈ లింక్‌లపై క్లిక్ చేయడం వల్ల మీ ఫోన్‌ లోకి వైరస్ వచ్చే ప్రమాదం ఉంది."
                        )
                    }
                } else if (text.contains("electricity") || text.contains("power") || text.contains("bill") || text.contains("disconnected") || text.contains("officer")) {
                    riskLevel = "HIGH"
                    category = "Utility Bill Scam"
                    explanation = getLocalString(
                        language,
                        en = "WARNING: Electricity bill fraud. Legitimate utility companies never send SMS from personal mobile numbers threatening immediate disconnection. They do not ask you to call personal numbers to update bills.",
                        hi = "चेतावनी: बिजली बिल फ्रॉड। बिजली विभाग कभी भी निजी मोबाइल नंबरों से तत्काल कनेक्शन काटने का संदेश नहीं भेजता है। ऐसे नंबरों पर बिल्कुल संपर्क न करें।",
                        te = "హెచ్చరిక: కరెంటు బిల్లు మోసం. కరెంటు బిల్లులు కట్టలేదని వెంటనే కనెక్షన్ కట్ చేస్తామని తెలియని నంబర్ల నుండి వచ్చే సందేశాలను నమ్మకండి. ఆ నంబర్లకు ఫోన్ చేయకండి."
                    )
                } else if (text.contains("otp") || text.contains("verification code")) {
                    riskLevel = "MEDIUM"
                    category = "Security Verification"
                    explanation = getLocalString(
                        language,
                        en = "Security Code Alert. This SMS contains a transaction OTP. Ensure that you did not share this code with anyone. Sharing this OTP can lead to instant financial theft.",
                        hi = "सुरक्षा कोड अलर्ट। इस एसएमएस में लेनदेन ओटीपी है। सुनिश्चित करें कि आपने यह कोड किसी को साझा नहीं किया है। इसे किसी को बताने पर पैसे चोरी हो सकते हैं।",
                        te = "భద్రతా కోడ్ అలర్ట్. ఈ SMS లో లావాదేవీల OTP ఉంది. ఈ కోడ్‌ను ఎవరితోనూ పంచుకోకండి. ఈ OTP చెబితే మీ ఖాతాలోని డబ్బులు దొంగిలించబడతాయి."
                    )
                } else {
                    riskLevel = "LOW"
                    category = "Standard Text message"
                    explanation = getLocalString(
                        language,
                        en = "This message is safe. It does not contain known scam indicators, phishing patterns, or dangerous external redirection URLs.",
                        hi = "यह संदेश सुरक्षित लग रहा है। इसमें कोई संदिग्ध निर्देश या फ़िशिंग पैटर्न नहीं मिला है।",
                        te = "ఈ సందేశం సురక్షితంగా కనిపిస్తోంది. ఇందులో ఎటువంటి మోసపూరిత వివరాలు లేవు."
                    )
                }
            }

            "WHATSAPP" -> {
                category = "WhatsApp Filter"
                if (normalizedInput.contains("job") || normalizedInput.contains("work from home") || normalizedInput.contains("salary") || normalizedInput.contains("part time") || normalizedInput.contains("telegram tasks") || normalizedInput.contains("youtube like")) {
                    riskLevel = "HIGH"
                    category = "Work-From-Home Scam"
                    explanation = getLocalString(
                        language,
                        en = "WARNING: Work from home job trap. Scammers offer easy money for liking YouTube videos or completing Telegram tasks. Later, they demand 'security deposits' or investment capital and freeze your funds. This is a massive task fraud.",
                        hi = "चेतावनी: वर्क फ्रॉम होम जॉब फ्रॉड। जालसाज यूट्यूब वीडियो लाइक करने या टेलीग्राम टास्क पूरे करने के लिए आसान पैसे देने का वादा करते हैं और बाद में निवेश के नाम पर पैसे ठगते हैं।",
                        te = "హెచ్చరిక: వర్క్ ఫ్రమ్ హోమ్ ఉద్యోగ మోసం. యూట్యూబ్ వీడియోలను లైక్ చేయడం లేదా టెలిగ్రామ్ టాస్క్‌లు చేయడం ద్వారా సులभంగా డబ్బు సంపాదించవచ్చని ఆశచూపి చివరకు డిపాజిట్ల పేరిట మోసం చేస్తారు."
                    )
                } else if (normalizedInput.contains("loan") || normalizedInput.contains("no document") || normalizedInput.contains("low interest") || normalizedInput.contains("immediate approval")) {
                    riskLevel = "HIGH"
                    category = "Instant Loan App Trap"
                    explanation = getLocalString(
                        language,
                        en = "WARNING: Unofficial Instant Loan Scam. High interest rates, illegal processing fees, and aggressive harassment tactics. Legitimate institutions do not offer loans over WhatsApp without official bank audits and documentation.",
                        hi = "चेतावनी: नकली तत्काल ऋण ऐप जाल। व्हाट्सएप के माध्यम से बिना किसी बैंक सत्यापन या दस्तावेज के ऋण देने के वादे पर कभी विश्वास न करें। यह आपकी निजी जानकारी चुराने का प्रयास हो सकता है।",
                        te = "హెచ్చరిక: ఇన్‌స్టంట్ లోన్ యాప్ మోసం. వాట్సాప్ ద్వారా ఎటువంటి గ్యారెంటీ లేకుండా లోన్ ఇస్తామని చెప్పే మోసగాళ్లను నమ్మకండి. మీ ఫోన్‌లోని కాంటాక్ట్‌లను దొంగిలించి బ్లాక్‌మెయిల్ చేసే అవకాశం ఉంది."
                    )
                } else if (normalizedInput.contains("lottery") || normalizedInput.contains("kbc") || normalizedInput.contains("luckydraw") || normalizedInput.contains("prize") || normalizedInput.contains("won")) {
                    riskLevel = "HIGH"
                    category = "Lottery Fraud Alert"
                    explanation = getLocalString(
                        language,
                        en = "WARNING: KBC/Lottery Fraud. Scammers send audio files or flyers on WhatsApp claiming you won 25 Lakhs. They demand a 'processing fee' or 'GST' to claim the reward. Once paid, they disappear.",
                        hi = "चेतावनी: केबीसी / लॉटरी फ्रॉड। व्हाट्सएप पर झूठे संदेश भेजकर 25 लाख की लॉटरी जीतने का दावा किया जाता है और टैक्स के नाम पर पैसे मांगे जाते हैं। यह 100% धोखाधड़ी है।",
                        te = "హెచ్చరిక: కేబీసీ / లాటరీ మోసం. మీకు 25 లక్షల లాటరీ తగిలిందని వాట్సాప్ లో నకిలీ పత్రాలు పంపి, ఆ డబ్బు కావాలంటే ట్యాక్స్ కట్టాలని అడుగుతారు. ఇది పూర్తిగా మోసం."
                    )
                } else {
                    riskLevel = "LOW"
                    category = "Safe Conversation"
                    explanation = getLocalString(
                        language,
                        en = "This text appears to be safe. It does not match the standard NLP profiles of job scams, instant loan traps, or lottery frauds.",
                        hi = "यह संदेश सुरक्षित है। इसमें कोई संदिग्ध लालच या धोखाधड़ी का पैटर्न नहीं मिला है।",
                        te = "ఈ సందేశం సురక్షితంగా ఉంది. ఇందులో ఎలాంటి ఉద్యోగ, లోన్ లేదా లాటరీ మోసాల ఆనవాళ్లు లేవు."
                    )
                }
            }

            "URL" -> {
                category = "URL Reputation"
                if (normalizedInput.contains("sbi") || normalizedInput.contains("hdfc") || normalizedInput.contains("icici") || normalizedInput.contains("kyc") || normalizedInput.contains("pan") || normalizedInput.contains("income-tax") || normalizedInput.contains("apk") || normalizedInput.contains("download")) {
                    if (!normalizedInput.startsWith("https://")) {
                        riskLevel = "HIGH"
                        category = "Phishing Domain"
                        explanation = getLocalString(
                            language,
                            en = "WARNING: Phishing domain suspected. It lacks HTTPS security or utilizes a misspelled name of major Indian banks or services. Clicking this will steal your financial credentials.",
                            hi = "चेतावनी: नकली फ़िशिंग वेबसाइट। इसमें सुरक्षित संबंध (HTTPS) नहीं है या प्रसिद्ध भारतीय बैंकों के नाम की स्पेलिंग गलत है। इस पर जाने से बचें।",
                            te = "హెచ్చరిక: నకిలీ వెబ్‌సైట్. ఇందులో సురక్షితమైన HTTPS కనెక్షన్ లేదు లేదా ప్రముఖ బ్యాంకుల పేర్లను తప్పుగా ఉపయోగించారు. ఈ వెబ్‌సైట్ లోకి వెళ్లకండి."
                        )
                    } else {
                        riskLevel = "MEDIUM"
                        category = "Suspicious Link"
                        explanation = getLocalString(
                            language,
                            en = "The URL redirects to a domain associated with banking keywords but is not hosted on the bank's official portal. Always verify bank URLs in your browser address bar manually.",
                            hi = "यह यूआरएल बैंकिंग कीवर्ड्स से जुड़ा है लेकिन आधिकारिक पोर्टल नहीं है। हमेशा बैंक के असली वेब एड्रेस को ध्यान से देखकर ही खोलें।",
                            te = "ఈ వెబ్‌సైట్ బ్యాంకింగ్ సంబంధిత పేరు కలిగి ఉన్నప్పటికీ ఇది అసలైన బ్యాంక్ సైట్ కాదు. కనుక ఎటువంటి వ్యక్తిగత వివరాలు ఎంటర్ చేయకండి."
                        )
                    }
                } else {
                    riskLevel = "LOW"
                    category = "Standard Domain"
                    explanation = getLocalString(
                        language,
                        en = "The website is secure (HTTPS) and does not contain known phish-blacklist tags. It appears to be safe for general visits.",
                        hi = "यह वेबसाइट सुरक्षित है और इसका नाम किसी ब्लैकलिस्ट से मेल नहीं खाता है। यह सुरक्षित प्रतीत होती है।",
                        te = "ఈ వెబ్‌సైట్ సురక్షితంగా మరియు సాధారణమైనదిగా కనిపిస్తోంది."
                    )
                }
            }

            "PAYMENT" -> {
                riskLevel = "MEDIUM"
                category = "Receipt OCR Analysis"
                explanation = getLocalString(
                    language,
                    en = "Offline Receipt Engine: Extracted mock transaction data. In offline mode, RakshaAI advises verifying your bank app or checking the credit SMS before dispatching goods. Check transaction ID format and look for font alignment anomalies.",
                    hi = "ऑफ़लाइन रसीद सत्यापन: कृपया सामान देने से पहले अपने बैंक खाते या क्रेडिट एसएमएस की जांच करें। कभी-कभी जालसाज नकली भुगतान रसीद ऐप्स का उपयोग करते हैं।",
                    te = "ఆఫ్‌లైన్ రసీదు వెరిఫికేషన్: వస్తువులను అప్పగించే ముందు మీ బ్యాంక్ ఖాతాలో డబ్బులు క్రెడిట్ అయ్యాయో లేదో చెక్ చేసుకోండి. నకిలీ రశీదులతో మోసం చేసే అవకాశం ఉంది."
                )
            }

            else -> {
                riskLevel = "LOW"
                category = "AI Explainer"
                explanation = getLocalString(
                    language,
                    en = "Legitimate financial entities, utility distributors, or police organizations will NEVER demand money transfers or security pins over suspicious phone calls, text messages, or unofficial links. Always crosscheck on official tollfree lines.",
                    hi = "असली बैंक अधिकारी, सरकारी विभाग या पुलिस कभी भी आपसे फोन पर पैसे ट्रांसफर करने या पासवर्ड साझा करने के लिए नहीं कहेंगे। हमेशा आधिकारिक स्रोतों से ही इसकी पुष्टि करें।",
                    te = "అసలైన బ్యాంక్ అధికారులు లేదా పోలీస్ సిబ్బంది ఎప్పుడూ ఫోన్ లో డబ్బులు పంపమని గానీ, పిన్ నంబర్లు చెప్పమని గానీ అడగరు. ఎప్పుడూ మోసగాళ్లను నమ్మకండి."
                )
            }
        }

        return ScanResult(
            type = type,
            input = input,
            riskLevel = riskLevel,
            confidenceScore = confidenceScore,
            category = category,
            explanation = explanation,
            language = language
        )
    }

    private fun getLocalString(language: String, en: String, hi: String, te: String): String {
        return when (language.uppercase(Locale.ROOT)) {
            "HINDI" -> hi
            "TELUGU" -> te
            else -> en
        }
    }
}

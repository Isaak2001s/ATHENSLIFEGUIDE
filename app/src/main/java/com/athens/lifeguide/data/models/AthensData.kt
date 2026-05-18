package com.athens.lifeguide.data.models

object AthensData {

    // ── Parks ─────────────────────────────────────────────────────────────
    val parks: List<Place> = listOf(
        Place("park_01", "Εθνικός Κήπος",           PlaceType.PARK,   37.9736, 23.7372, "Το εμβληματικό πάρκο της Αθήνας, 15,5 εκτάρια πρασίνου δίπλα στο Σύνταγμα"),
        Place("park_02", "Πεδίον του Άρεως",         PlaceType.PARK,   37.9893, 23.7344, "Το μεγαλύτερο πάρκο της Αθήνας, 27 εκτάρια στην Πατησίων"),
        Place("park_03", "Λόφος Φιλοπάππου",         PlaceType.PARK,   37.9672, 23.7218, "Αρχαιολογικός λόφος με πανοραμική θέα στην Ακρόπολη"),
        Place("park_04", "Λόφος Στρέφη",             PlaceType.PARK,   37.9897, 23.7299, "Πράσινος λόφος στα Εξάρχεια με θέα στην πόλη"),
        Place("park_05", "Άλσος Βεΐκου",             PlaceType.PARK,   38.0031, 23.7467, "Δασώδες άλσος 55 εκταρίων στο Γαλάτσι"),
        Place("park_06", "Πάρκο Αντώνης Τρίτσης",   PlaceType.PARK,   37.9650, 23.6817, "Μεγάλο οικολογικό πάρκο 250 εκταρίων στο Ίλιον"),
        Place("park_07", "Πάρκο Ιλισίων",            PlaceType.PARK,   37.9820, 23.7580, "Ήσυχο τοπικό πάρκο στα Ιλίσια"),
        Place("park_08", "Αττικό Άλσος",             PlaceType.PARK,   38.0033, 23.7200, "Μεγάλο δάσος-πάρκο στο Περισσό"),
        Place("park_09", "Άλσος Νέας Φιλαδέλφειας", PlaceType.PARK,   38.0256, 23.7433, "Αστικό άλσος με παιδικές χαρές"),
        Place("park_10", "Λόφος Νυμφών",             PlaceType.PARK,   37.9703, 23.7192, "Αρχαιολογικός λόφος δίπλα στο Θησείο")
    )

    // ── Squares ───────────────────────────────────────────────────────────
    val squares: List<Place> = listOf(
        Place("sq_01", "Πλατεία Συντάγματος",   PlaceType.SQUARE, 37.9754, 23.7352, "Η κεντρικότερη πλατεία της Αθήνας, μπροστά στη Βουλή"),
        Place("sq_02", "Πλατεία Μοναστηρακίου", PlaceType.SQUARE, 37.9755, 23.7255, "Εμβληματική αγορά με θέα στην Ακρόπολη"),
        Place("sq_03", "Πλατεία Εξαρχείων",     PlaceType.SQUARE, 37.9886, 23.7316, "Ζωντανή πλατεία, καρδιά της εναλλακτικής Αθήνας"),
        Place("sq_04", "Πλατεία Κολωνακίου",    PlaceType.SQUARE, 37.9793, 23.7422, "Κομψή αστική πλατεία με καφέ και μπουτίκ"),
        Place("sq_05", "Πλατεία Βικτωρίας",     PlaceType.SQUARE, 37.9944, 23.7338, "Πολυπολιτισμική πλατεία στο κέντρο"),
        Place("sq_06", "Πλατεία Ομόνοιας",      PlaceType.SQUARE, 37.9838, 23.7279, "Η μεγάλη κεντρική πλατεία με το σιντριβάνι"),
        Place("sq_07", "Πλατεία Αγ. Ειρήνης",   PlaceType.SQUARE, 37.9762, 23.7261, "Trendy πλατεία στο Μοναστηράκι"),
        Place("sq_08", "Πλατεία Κεραμεικού",    PlaceType.SQUARE, 37.9758, 23.7193, "Αναπλασμένη πλατεία κοντά στον αρχαιολογικό χώρο"),
        Place("sq_09", "Πλατεία Μαβίλη",        PlaceType.SQUARE, 37.9824, 23.7458, "Γαστρονομική πλατεία στα Αμπελόκηπια"),
        Place("sq_10", "Πλατεία Κυψέλης",       PlaceType.SQUARE, 37.9971, 23.7373, "Ανακαινισμένη πλατεία στην Κυψέλη")
    )

    // ── Transit ───────────────────────────────────────────────────────────
    val transit: List<Place> = listOf(
        // Metro
        Place("m_syntagma",    "Σύνταγμα",       PlaceType.METRO, 37.9750, 23.7355, "Κεντρικός κόμβος – Γραμμές 2 & 3", listOf("Μ2","Μ3")),
        Place("m_monastiraki", "Μοναστηράκι",    PlaceType.METRO, 37.9757, 23.7261, "Σύνδεση Γραμμών 1 & 3",            listOf("Μ1","Μ3")),
        Place("m_omonia",      "Ομόνοια",        PlaceType.METRO, 37.9840, 23.7285, "Σύνδεση Γραμμών 1 & 2",            listOf("Μ1","Μ2")),
        Place("m_attiki",      "Αττική",         PlaceType.METRO, 38.0004, 23.7293, "Σύνδεση Γραμμών 1 & 2",            listOf("Μ1","Μ2")),
        Place("m_acropolis",   "Ακρόπολη",       PlaceType.METRO, 37.9686, 23.7286, "Σταθμός Γραμμής 2 – Κοντά στο μνημείο", listOf("Μ2")),
        Place("m_evangelismos","Ευαγγελισμός",   PlaceType.METRO, 37.9763, 23.7461, "Σταθμός Γραμμής 3",                listOf("Μ3")),
        Place("m_larissa",     "Σταθμός Λαρίσης",PlaceType.METRO, 37.9951, 23.7215, "Σύνδεση τρένου & μετρό",          listOf("Μ2")),
        Place("m_kifissia",    "Κηφισιά",        PlaceType.METRO, 38.0731, 23.8120, "Τερματικός σταθμός Γραμμής 1",    listOf("Μ1")),
        Place("m_piraeus",     "Πειραιάς",       PlaceType.METRO, 37.9428, 23.6464, "Τερματικός σταθμός Γραμμών 1 & 3",listOf("Μ1","Μ3")),
        Place("m_airport",     "Αεροδρόμιο",     PlaceType.METRO, 37.9367, 23.9445, "Τερματικός Γραμμής 3 – Ελ. Βενιζέλος", listOf("Μ3")),
        Place("m_douk_plak",   "Δουκ. Πλακεντίας",PlaceType.METRO,37.9837, 23.8078,"Σταθμός Γραμμής 3",               listOf("Μ3")),
        Place("m_holargos",    "Χολαργός",       PlaceType.METRO, 37.9952, 23.8009, "Σταθμός Γραμμής 3",               listOf("Μ3")),
        // Tram
        Place("t_syntagma",    "Σύνταγμα (Τραμ)",PlaceType.TRAM,  37.9753, 23.7348, "Αφετηρία τραμ για Φάληρο & Βούλα", listOf("Τ3","Τ4")),
        Place("t_neofaliro",   "Νέο Φάληρο",     PlaceType.TRAM,  37.9391, 23.6818, "Παράκτιος σταθμός τραμ",          listOf("Τ3")),
        Place("t_voula",       "Βούλα",          PlaceType.TRAM,  37.8591, 23.7341, "Νότιος τερματικός σταθμός τραμ",  listOf("Τ4"))
    )

    // ── Map centre & zoom defaults ─────────────────────────────────────────
    const val LAT  = 37.9838
    const val LNG  = 23.7275
    const val ZOOM = 13
    // ── Parking ───────────────────────────────────────────────────────────
    val parking: List<ParkingSpot> = listOf(
        ParkingSpot("park_syntagma",    "Syntagma Square Parking",    37.9755, 23.7348, 50, 23, 2.5, "Syntagma Sq, Athens",    "medium"),
        ParkingSpot("park_kolonaki",    "Kolonaki Parking Garage",    37.9787, 23.7441, 30,  5, 3.5, "Kolonaki Sq, Athens",    "high"),
        ParkingSpot("park_monastiraki", "Monastiraki Open Lot",       37.9762, 23.7258, 40, 32, 2.0, "Monastiraki, Athens",    "low"),
        ParkingSpot("park_plaka",       "Plaka Underground",          37.9720, 23.7308, 60, 15, 3.0, "Plaka, Athens",          "medium"),
        ParkingSpot("park_omonia",      "Omonia Square Lot",          37.9838, 23.7275, 80, 67, 1.8, "Omonia Sq, Athens",      "low"),
        ParkingSpot("park_thissio",     "Thissio Parking",            37.9762, 23.7205, 35, 28, 2.0, "Thissio, Athens",        "low")
    )
}

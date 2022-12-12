package com.example.ocrdemo2

class CardDetails {

    companion object {
        const val CARD_NUMBER_REGEX = "[\\d?]{12,19}"
        const val EXPIRY_DATE_REGEX = "(?:0[1-9]|1[0-2])/\\d{2}"
    }

    var cardNumber: String = ""
        set(value) {
            if (value.matches(Regex(CARD_NUMBER_REGEX))) {
                field = value
                return
            }
            field = ""
        }
    var expiryDate: String = ""
        set(value) {
            if (value.matches(Regex(EXPIRY_DATE_REGEX))) {
                field = value
                return
            }
            field = ""
        }

    fun isValid(): Boolean {
        return cardNumber.isNotEmpty() && expiryDate.isNotEmpty()
    }
}
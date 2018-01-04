package io.github.ubuntudroid.toggltracker.network.model

import com.google.gson.annotations.SerializedName

data class Summary(
        /**
         * total time in milliseconds for the selected report
         */
        @SerializedName("total_grand")
        val totalGrand: Long = 0,

        /**
         * total billable time in milliseconds for the selected report
         */
        @SerializedName("total_billable")
        val totalBillable: Long = 0
)
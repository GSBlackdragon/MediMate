package com.example.mms.model.medicines

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Medicine constructor(
    var name: String,
    @PrimaryKey
    var code_cis: Long,
    var code_has: String?,

    @Embedded(prefix = "type_")
    var type: MType,
    @Embedded(prefix = "sales_info_")
    var sales_info: SalesInfos?,
    @Embedded(prefix = "usage_")
    var usage: Usage?,
    @Embedded(prefix = "composition_")
    var composition: Composition?,
    @Embedded(prefix = "security_info_")
    var security_informations: SecurityInformations?,
    @Embedded(prefix = "availability_")
    var availability: Availbility?,
    @Embedded(prefix = "generic_group_")
    var generic_group: GenericGroup?,
) {

    constructor() : this(
        "",
        0,
        null,
        MType(),
        SalesInfos(),
        Usage(),
        Composition(),
        SecurityInformations(),
        Availbility(),
        GenericGroup()
    )

    constructor(name: String, cis: Long) : this(
        name,
        cis,
        null,
        MType(),
        SalesInfos(),
        Usage(),
        Composition(),
        SecurityInformations(),
        Availbility(),
        GenericGroup()
    )

    override fun toString(): String {
        return "${this.name} - ${this.code_cis}: ${this.type.generic}, ${this.type.weight}"
    }
}

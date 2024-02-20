package com.example.mms.database.mongoObjects

import com.example.mms.model.Version
import kotlinx.serialization.Serializable

/**
 * model to store the version and the updated documents cis
 *
 * @property version The version of the database
 * @property updatedDocumentsCis The cis of the updated documents
 */
@Serializable
class MongoVersion (
    var version: Int,
    var updatedDocumentsCis: List<Int>,
) {
    /**
     * Converts the MongoVersion to a Version
     *
     * @return The converted Version
     */
    fun toVersion(): Version {
        return Version(0, this.version)
    }

    /**
     * String representation of the MongoVersion
     *
     * @return The string representation of the MongoVersion
     */
    override fun toString(): String {
        return "MongoVersion(version=$version, updated_documents_cis=${updatedDocumentsCis.size})"
    }
}

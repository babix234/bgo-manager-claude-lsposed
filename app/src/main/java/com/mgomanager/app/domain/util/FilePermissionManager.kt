package com.mgomanager.app.domain.util

import javax.inject.Inject
import javax.inject.Singleton

data class FilePermissions(
    val owner: String,
    val group: String,
    val permissions: String
)

@Singleton
class FilePermissionManager @Inject constructor(
    private val rootUtil: RootUtil
) {

    /**
     * Read file ownership and permissions
     */
    suspend fun getFilePermissions(path: String): Result<FilePermissions> {
        val result = rootUtil.executeCommand("stat -c '%U:%G %a' $path")

        return result.mapCatching { output ->
            // Expected format: "u0_a123:u0_a123 755"
            val parts = output.trim().split(" ")
            if (parts.size != 2) {
                throw Exception("Unexpected stat output: $output")
            }

            val ownerGroup = parts[0].split(":")
            if (ownerGroup.size != 2) {
                throw Exception("Unexpected owner:group format: ${parts[0]}")
            }

            FilePermissions(
                owner = ownerGroup[0],
                group = ownerGroup[1],
                permissions = parts[1]
            )
        }
    }

    /**
     * Set file ownership
     */
    suspend fun setFileOwnership(path: String, owner: String, group: String): Result<Unit> {
        return rootUtil.executeCommand("chown -R $owner:$group $path").map { }
    }

    /**
     * Set file permissions
     */
    suspend fun setFilePermissions(path: String, permissions: String): Result<Unit> {
        return rootUtil.executeCommand("chmod -R $permissions $path").map { }
    }
}

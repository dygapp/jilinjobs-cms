package com.jilinjobs.cms.audit

enum class AdminAuditAction {
    CREATE,
    UPDATE,
    DELETE,
    PUBLISH,
    WITHDRAW,
    UPLOAD,
    REPLACE,
    TRASH,
    RESTORE,
}

enum class AdminAuditObjectType {
    COLUMN,
    ARTICLE,
    NAVIGATION_LOCATION,
    NAVIGATION_ITEM,
    PAGE_GROUP,
    PAGE,
    CMS_LIST,
    CMS_LIST_ITEM,
    ADVERTISEMENT_SLOT,
    ADVERTISEMENT_ITEM,
    SITE_PROPERTY,
    MANAGED_RESOURCE,
    STATIC_RESOURCE,
}

enum class AdminAuditObjectIdSource {
    RETURN_PROPERTY,
    ARGUMENT,
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AdminAuditOperation(
    val action: AdminAuditAction,
    val objectType: AdminAuditObjectType,
    val objectIdSource: AdminAuditObjectIdSource = AdminAuditObjectIdSource.RETURN_PROPERTY,
    val objectIdParameter: String = "",
    val returnProperty: String = "id",
    val fallbackObjectIdParameter: String = "",
    val alternateAction: AdminAuditAction = AdminAuditAction.UPLOAD,
    val alternateActionParameter: String = "",
)

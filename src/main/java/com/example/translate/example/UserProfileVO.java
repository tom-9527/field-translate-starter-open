package com.example.translate.example;

import com.example.translate.annotation.TranslateField;
import com.example.translate.annotation.TranslateType;

/**
 * Example VO for demonstrating field translation.
 * <p>
 * Design intent: show how to declare translation intent on raw fields while
 * keeping display fields separate to avoid overwriting original values.
 * </p>
 */
public class UserProfileVO {

    /**
     * Raw status code returned by business logic.
     * <p>
     * Design intent: business code writes only the raw value; translation
     * happens later at the response boundary.
     * </p>
     */
    @TranslateField(type = TranslateType.ENUM, enumClass = UserStatus.class, target = "statusName")
    private Integer status;

    /**
     * Display field for translated status.
     * <p>
     * Design intent: separate display value to prevent field overwrite.
     * </p>
     */
    private String statusName;

    /**
     * Raw department code from business data.
     * <p>
     * Design intent: declare cache-based translation by dict key.
     * </p>
     */
    @TranslateField(type = TranslateType.CACHE, dictKey = "dept", target = "deptName")
    private String deptCode;

    /**
     * Display field for translated department name.
     */
    private String deptName;

    /**
     * Raw organization id that needs table translation.
     */
    @TranslateField(type = TranslateType.TABLE,
            table = "org",
            keyColumn = "id",
            valueColumn = "name",
            target = "orgName")
    private Long orgId;

    /**
     * Display field for translated organization name.
     */
    private String orgName;

    /**
     * Raw code to be translated by an external service.
     */
    @TranslateField(type = TranslateType.RPC, rpcService = "user-profile", rpcMethod = "batchName", target = "remoteName")
    private String remoteCode;

    /**
     * Display field for RPC translation result.
     */
    private String remoteName;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getRemoteCode() {
        return remoteCode;
    }

    public void setRemoteCode(String remoteCode) {
        this.remoteCode = remoteCode;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }
}

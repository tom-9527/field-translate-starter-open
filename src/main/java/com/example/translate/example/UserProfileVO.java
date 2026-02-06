package com.example.translate.example;

import com.example.translate.annotation.TranslateField;
import com.example.translate.annotation.TranslateType;

/**
 * 字段翻译示例 VO。
 * <p>
 * 设计意图：演示在原始字段上声明翻译意图，
 * 同时将展示字段分离以避免覆盖原始值。
 * </p>
 */
public class UserProfileVO {

    /**
     * 业务逻辑返回的原始状态码。
     * <p>
     * 设计意图：业务代码只写原值，翻译在响应边界再执行。
     * </p>
     */
    @TranslateField(type = TranslateType.ENUM, enumClass = UserStatus.class, target = "statusName")
    private Integer status;

    /**
     * 翻译后的状态展示字段。
     * <p>
     * 设计意图：分离展示值，避免字段覆盖。
     * </p>
     */
    private String statusName;

    /**
     * 业务数据中的原始部门编码。
     * <p>
     * 设计意图：通过字典 key 声明缓存翻译。
     * </p>
     */
    @TranslateField(type = TranslateType.CACHE, dictKey = "dept", target = "deptName")
    private String deptCode;

    /**
     * 翻译后的部门名称展示字段。
     */
    private String deptName;

    /**
     * 需要表翻译的原始组织 id。
     */
    @TranslateField(type = TranslateType.TABLE,
            table = "org",
            keyColumn = "id",
            valueColumn = "name",
            target = "orgName")
    private Long orgId;

    /**
     * 翻译后的组织名称展示字段。
     */
    private String orgName;

    /**
     * 由外部服务翻译的原始 code。
     */
    @TranslateField(type = TranslateType.RPC, rpcService = "user-profile", rpcMethod = "batchName", target = "remoteName")
    private String remoteCode;

    /**
     * RPC 翻译结果展示字段。
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

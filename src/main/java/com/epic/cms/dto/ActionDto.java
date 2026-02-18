package com.epic.cms.dto;

import jakarta.validation.constraints.NotNull;

public class ActionDto {

    @NotNull
    private Boolean approve;

    public ActionDto() {}

    public ActionDto(Boolean approve) {
        this.approve = approve;
    }

    public Boolean getApprove() {
        return approve;
    }

    public void setApprove(Boolean approve) {
        this.approve = approve;
    }
}

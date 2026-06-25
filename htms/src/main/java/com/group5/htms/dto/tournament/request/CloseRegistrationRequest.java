package com.group5.htms.dto.tournament.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CloseRegistrationRequest {
    private Boolean autoRejectPending = true;
    private Boolean autoCancelUnconfirmed = false;

    public boolean isAutoRejectPending() {
        return autoRejectPending == null || autoRejectPending;
    }

    public boolean isAutoCancelUnconfirmed() {
        return Boolean.TRUE.equals(autoCancelUnconfirmed);
    }
}

package com.contactmaster.dto;

public class DashboardDtos {
    public record Statistics(long totalContacts, long favoriteContacts, long groupCount, long recycleBinCount, long birthdayCount) {
    }
}

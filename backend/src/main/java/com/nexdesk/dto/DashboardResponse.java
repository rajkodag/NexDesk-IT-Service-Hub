package com.nexdesk.dto;

import java.util.Map;

public class DashboardResponse {

    private long totalTickets;
    private long openTickets;
    private long assignedTickets;
    private long inProgressTickets;
    private long resolvedTickets;
    private long closedTickets;
    private long slaBreachedTickets;

    private double mttrHours;
    private String slaComplianceRate;
    private String firstContactResolutionRate;

    private Map<String, Long> categoryDistribution;
    private Map<String, Long> priorityDistribution;

    public DashboardResponse() {
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(long openTickets) {
        this.openTickets = openTickets;
    }

    public long getAssignedTickets() {
        return assignedTickets;
    }

    public void setAssignedTickets(long assignedTickets) {
        this.assignedTickets = assignedTickets;
    }

    public long getInProgressTickets() {
        return inProgressTickets;
    }

    public void setInProgressTickets(long inProgressTickets) {
        this.inProgressTickets = inProgressTickets;
    }

    public long getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public long getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(long closedTickets) {
        this.closedTickets = closedTickets;
    }

    public long getSlaBreachedTickets() {
        return slaBreachedTickets;
    }

    public void setSlaBreachedTickets(long slaBreachedTickets) {
        this.slaBreachedTickets = slaBreachedTickets;
    }

    public double getMttrHours() {
        return mttrHours;
    }

    public void setMttrHours(double mttrHours) {
        this.mttrHours = mttrHours;
    }

    public String getSlaComplianceRate() {
        return slaComplianceRate;
    }

    public void setSlaComplianceRate(String slaComplianceRate) {
        this.slaComplianceRate = slaComplianceRate;
    }

    public String getFirstContactResolutionRate() {
        return firstContactResolutionRate;
    }

    public void setFirstContactResolutionRate(String firstContactResolutionRate) {
        this.firstContactResolutionRate = firstContactResolutionRate;
    }

    public Map<String, Long> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(Map<String, Long> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }

    public Map<String, Long> getPriorityDistribution() {
        return priorityDistribution;
    }

    public void setPriorityDistribution(Map<String, Long> priorityDistribution) {
        this.priorityDistribution = priorityDistribution;
    }
}

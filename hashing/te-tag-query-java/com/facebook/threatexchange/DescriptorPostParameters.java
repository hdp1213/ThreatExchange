package com.facebook.threatexchange;

import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Helper container class for posting threat descriptors to ThreatExchange.
 */
class DescriptorPostParameters {
  private String _indicatorText;
  private String _indicatorType;
  private String _description;
  private String _shareLevel;
  private String _status;
  private String _privacyType;
  // If privacy_type is HAS_WHITELIST these must be app IDs.
  // If privacy_type is HAS_PRIVACY_GROUP these must be privacy-group IDs.
  private String _privacyMembers;

  private String _confidence;
  private String _precision;
  private String _reviewStatus;
  private String _severity;
  private String _expiredOn;
  private String _firstActive;
  private String _lastActive;
  private String _tagsToSet;
  private String _tagsToAdd;
  private String _tagsToRemove;

  public DescriptorPostParameters setIndicatorText(String indicatorText) {
    this._indicatorText = indicatorText;
    return this;
  }
  public DescriptorPostParameters setIndicatorType(String indicatorType) {
    this._indicatorType = indicatorType;
    return this;
  }
  public DescriptorPostParameters setDescription(String description) {
    this._description = description;
    return this;
  }
  public DescriptorPostParameters setShareLevel(String shareLevel) {
    this._shareLevel = shareLevel;
    return this;
  }
  public DescriptorPostParameters setStatus(String status) {
    this._status = status;
    return this;
  }
  public DescriptorPostParameters setPrivacyType(String privacyType) {
    this._privacyType = privacyType;
    return this;
  }
  public DescriptorPostParameters setPrivacyMembers(String privacyMembers) {
    this._privacyMembers = privacyMembers;
    return this;
  }
  public DescriptorPostParameters setConfidence(String confidence) {
    this._confidence = confidence;
    return this;
  }
  public DescriptorPostParameters setPrecision(String precision) {
    this._precision = precision;
    return this;
  }
  public DescriptorPostParameters setReviewStatus(String reviewStatus) {
    this._reviewStatus = reviewStatus;
    return this;
  }
  public DescriptorPostParameters setSeverity(String severity) {
    this._severity = severity;
    return this;
  }
  public DescriptorPostParameters setExpiredOn(String expiredOn) {
    this._expiredOn = expiredOn;
    return this;
  }
  public DescriptorPostParameters setFirstActive(String firstActive) {
    this._firstActive = firstActive;
    return this;
  }
  public DescriptorPostParameters setLastActive(String lastActive) {
    this._lastActive = lastActive;
    return this;
  }
  public DescriptorPostParameters setTagsToSet(String tagsToSet) {
    this._tagsToSet = tagsToSet;
    return this;
  }
  public DescriptorPostParameters setTagsToAdd(String tagsToAdd) {
    this._tagsToAdd = tagsToAdd;
    return this;
  }
  public DescriptorPostParameters setTagsToRemove(String tagsToRemove) {
    this._tagsToRemove = tagsToRemove;
    return this;
  }

  public String getIndicatorText() {
    return this._indicatorText;
  }
  public String getIndicatorType() {
    return this._indicatorType;
  }
  public String getDescription() {
    return this._description;
  }
  public String getShareLevel() {
    return this._shareLevel;
  }
  public String getStatus() {
    return this._status;
  }
  public String getPrivacyType() {
    return this._privacyType;
  }
  public String getPrivacyMembers() {
    return this._privacyMembers;
  }
  public String getConfidence() {
    return this._confidence;
  }
  public String getPrecision() {
    return this._precision;
  }
  public String getReviewStatus() {
    return this._reviewStatus;
  }
  public String getSeverity() {
    return this._severity;
  }
  public String getExpiredOn() {
    return this._expiredOn;
  }
  public String getFirstActive() {
    return this._firstActive;
  }
  public String getLastActive() {
    return this._lastActive;
  }
  public String getTagsToSet() {
    return this._tagsToSet;
  }
  public String getTagsToAdd() {
    return this._tagsToAdd;
  }
  public String getTagsToRemove() {
    return this._tagsToRemove;
  }

  public boolean validateWithReport(PrintStream o) {
    if (this._indicatorText == null) {
      System.err.println("Indicator text is missing.\n");
      return false;
    }
    if (this._indicatorType == null) {
      System.err.println("Indicator type is missing.\n");
      return false;
    }
    if (this._description == null) {
      System.err.println("Description is missing.\n");
      return false;
    }
    if (this._shareLevel == null) {
      System.err.println("Share level is missing.\n");
      return false;
    }
    if (this._status == null) {
      System.err.println("Status is missing.\n");
      return false;
    }
    if (this._privacyType == null) {
      System.err.println("Privacy type is missing.\n");
      return false;
    }
    return true;
  }

  // URL-encode since data is user-provided.
  // Assumes the input is already validated (non-null indicator text/type etc.)
  public String getPostDataString() {
    StringBuilder sb = new StringBuilder();
    sb.append("type=").append(Utils.urlEncodeUTF8(this._indicatorType));
    sb.append("&description=").append(Utils.urlEncodeUTF8(this._description));
    sb.append("&share_level=").append(Utils.urlEncodeUTF8(this._shareLevel));
    sb.append("&status=").append(Utils.urlEncodeUTF8(this._status));
    sb.append("&privacy_type=").append(Utils.urlEncodeUTF8(this._privacyType));
    if (this._privacyMembers != null) {
      sb.append("&privacy_members=").append(Utils.urlEncodeUTF8(this._privacyMembers));
    }
    if (this._tagsToSet != null) {
      sb.append("&tags=").append(Utils.urlEncodeUTF8(this._tagsToSet));
    }
    if (this._tagsToAdd != null) {
      sb.append("&add_tags=").append(Utils.urlEncodeUTF8(this._tagsToAdd));
    }
    if (this._tagsToRemove != null) {
      sb.append("&remove_tags=").append(Utils.urlEncodeUTF8(this._tagsToRemove));
    }
    if (this._confidence != null) {
      sb.append("&confidence=").append(Utils.urlEncodeUTF8(this._confidence));
    }
    if (this._precision != null) {
      sb.append("&precision=").append(Utils.urlEncodeUTF8(this._precision));
    }
    if (this._reviewStatus != null) {
      sb.append("&review_status=").append(Utils.urlEncodeUTF8(this._reviewStatus));
    }
    if (this._severity != null) {
      sb.append("&severity=").append(Utils.urlEncodeUTF8(this._severity));
    }
    if (this._expiredOn != null) {
      sb.append("&expired_on=").append(Utils.urlEncodeUTF8(this._expiredOn));
    }
    if (this._firstActive != null) {
      sb.append("&first_active=").append(Utils.urlEncodeUTF8(this._firstActive));
    }
    if (this._lastActive != null) {
      sb.append("&last_active=").append(Utils.urlEncodeUTF8(this._tagsToRemove));
    }
    // Put indicator last in case it's long (e.g. TMK) for human readability
    sb.append("&indicator=").append(Utils.urlEncodeUTF8(this._indicatorText));
    return sb.toString();
  }
}

/*
 * Copyright (c) 2025. PortSwigger Ltd. All rights reserved.
 *
 * This code may be used to extend the functionality of Burp Suite Community Edition
 * and Burp Suite Professional, provided that this usage does not violate the
 * license terms for those products.
 */

package com.veggiespam.imagelocationscanner;
import burp.api.montoya.MontoyaApi;


import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.MimeType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
    
import burp.api.montoya.scanner.AuditResult;
import burp.api.montoya.scanner.ConsolidationAction;
import burp.api.montoya.scanner.audit.issues.AuditIssue;
import burp.api.montoya.scanner.audit.issues.AuditIssueConfidence;
import burp.api.montoya.scanner.audit.issues.AuditIssueSeverity;
import burp.api.montoya.scanner.scancheck.PassiveScanCheck;

import burp.api.montoya.logging.Logging;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import static burp.api.montoya.scanner.AuditResult.auditResult;
import static burp.api.montoya.scanner.ConsolidationAction.KEEP_BOTH;
import static burp.api.montoya.scanner.ConsolidationAction.KEEP_EXISTING;
import static burp.api.montoya.scanner.audit.issues.AuditIssue.auditIssue;
import static com.veggiespam.imagelocationscanner.Utilities.getResponseHighlights;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.veggiespam.imagelocationscanner.ILS;

public class BurpPassiveScan implements PassiveScanCheck
{
    private static final String modName = ILS.pluginName;
    private static final String auditIssueName = ILS.alertTitle;
    private static final String issueDetailPrefix = ILS.alertDetailPrefix;
    private static final String issueBackground  = ILS.alertBackground;
    private static final String remediationBackground = ILS.remediationBackground;
    private static final String remediationDetail = ILS.remediationDetail;

        /** Used in some debug statements. */
    private static final String SEP = " | ";

    @Override
    public String checkName()
    {
        return ILS.pluginName;
    }

    @Override
    public AuditResult doCheck(HttpRequestResponse httpRequestResponse)
    {
        String findings = "";
        List<String> mimeExtensionList = new ArrayList<String>(Arrays.asList("jpeg", "jpg", "png", "heif", "heic", "tiff", "tif"));
        List<MimeType> mimeTypeList = new ArrayList<MimeType>(Arrays.asList(MimeType.IMAGE_JPEG, MimeType.IMAGE_PNG));

       //MontoyaApi api = this.getApi();
        //Logging logging = api.logging();



        HttpRequest request = httpRequestResponse.request();
        HttpResponse response = httpRequestResponse.response();

              //  logging.logToOutput("Checking request: " + request.url());

        /* We try to detect the file type three ways.
         * 1.  Burp's inferred mime type, which used to work perfectly in 2019 and broke in 2025.
         * 2.  The HTTP Header mime type, which is what the server tells us the file is.
         * 3.  The file extension.
         */
        //String contentType = response.header("Content-Type");
        //String mimeHeader = response.mimeType();
        MimeType mimeInferred = response.inferredMimeType();
        MimeType mimeStated = response.statedMimeType();
        String fileName = request.url();       
        String extension = request.fileExtension();

        //logging.logToOutput("mimeStated: " + mimeStated + SEP + "mimeInferred: " + mimeInferred + SEP + "ext: " + extension + SEP + fileName);

		if ( mimeTypeList.contains(mimeInferred) ||  mimeTypeList.contains(mimeStated) || mimeExtensionList.contains(extension) ) {
            findings = ILS.scanForLocationInImageHTML(httpRequestResponse.response().body().getBytes());
        }

        List<AuditIssue> auditIssueList = findings.isEmpty() ? emptyList() : singletonList(
                auditIssue(
                        auditIssueName,
                        findings,
                        null, // leave null, we have no detailed remediation steps
                        request.url(),
                        AuditIssueSeverity.INFORMATION,
                        AuditIssueConfidence.CERTAIN,
                        issueBackground, 
                        remediationBackground,
                        AuditIssueSeverity.INFORMATION, 
                        httpRequestResponse
                )
        );

        return auditResult(auditIssueList);
    }

    @Override
    public ConsolidationAction consolidateIssues(AuditIssue existingIssue, AuditIssue newIssue)
    {
        return existingIssue.name().equals(newIssue.name()) ? KEEP_EXISTING : KEEP_BOTH;
    }
}
package com.vplay.live;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.FormBody;
import okhttp3.RequestBody;

public class WebViewRequestHelper {
    private static final String TAG = "WebViewRequestHelper";
    
    public interface WebViewCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    public static void makeWebViewRequest(Context context, String url, RequestBody requestBody, WebViewCallback callback) {
        if (!(context instanceof AppCompatActivity)) {
            callback.onError("WebView requires Activity context");
            return;
        }
        
        AppCompatActivity activity = (AppCompatActivity) context;
        
        activity.runOnUiThread(() -> {
            try {
                WebView webView = new WebView(context);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setUserAgentString("CineStreamApp/1.0");
                webView.setVisibility(View.GONE); // Oculta a WebView
                
                // Add JavaScript interface
                webView.addJavascriptInterface(new Object() {
                    @JavascriptInterface
                    public void processContent(final String html) {
                        Log.d(TAG, "WebView content received: " + html.substring(0, Math.min(html.length(), 200)));
                        
                        activity.runOnUiThread(() -> {
                            // Clean up
                            webView.destroy();
                            callback.onSuccess(html);
                        });
                    }
                    
                    @JavascriptInterface
                    public void onError(String error) {
                        Log.e(TAG, "WebView JavaScript error: " + error);
                        
                        activity.runOnUiThread(() -> {
                            // Clean up
                            webView.destroy();
                            callback.onError(error);
                        });
                    }
                }, "AndroidBridge");
                
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String loadedUrl) {
                        Log.d(TAG, "WebView page finished loading: " + loadedUrl);
                        
                        // Extract response content using JavaScript
                        String javascript = "javascript:" +
                                "try {" +
                                "  var content = document.body.innerText || document.body.textContent || '';" +
                                "  window.AndroidBridge.processContent(content);" +
                                "} catch(e) {" +
                                "  window.AndroidBridge.onError('JavaScript execution error: ' + e.message);" +
                                "}";
                        
                        webView.loadUrl(javascript);
                    }
                    
                    @Override
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        Log.e(TAG, "WebView error " + errorCode + ": " + description + " at " + failingUrl);
                        
                        activity.runOnUiThread(() -> {
                            webView.destroy();
                            callback.onError("WebView error: " + description);
                        });
                    }
                });
                
                // Create form data for POST request
                String postData = createPostDataFromRequestBody(requestBody);
                
                Log.d(TAG, "Making WebView POST request to: " + url);
                Log.d(TAG, "POST data: " + postData);
                
                if (!postData.isEmpty()) {
                    // Make POST request
                    webView.postUrl(url, postData.getBytes());
                } else {
                    // Fallback to GET request
                    webView.loadUrl(url);
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error creating WebView request", e);
                callback.onError("Error creating WebView: " + e.getMessage());
            }
        });
    }
    
    private static String createPostDataFromRequestBody(RequestBody requestBody) {
        if (requestBody instanceof FormBody) {
            FormBody formBody = (FormBody) requestBody;
            StringBuilder postData = new StringBuilder();
            
            for (int i = 0; i < formBody.size(); i++) {
                if (i > 0) postData.append("&");
                
                String name = formBody.name(i);
                String value = formBody.value(i);
                
                // URL encode the values
                try {
                    name = java.net.URLEncoder.encode(name, "UTF-8");
                    value = java.net.URLEncoder.encode(value, "UTF-8");
                } catch (java.io.UnsupportedEncodingException e) {
                    Log.e(TAG, "Error encoding form data", e);
                }
                
                postData.append(name).append("=").append(value);
            }
            
            return postData.toString();
        }
        
        return "";
    }
    
    /**
     * Alternative WebView implementation using HTML form submission
     * This method creates an HTML page with a form and auto-submits it
     */
    public static void makeWebViewFormRequest(Context context, String url, RequestBody requestBody, WebViewCallback callback) {
        if (!(context instanceof AppCompatActivity)) {
            callback.onError("WebView requires Activity context");
            return;
        }
        
        AppCompatActivity activity = (AppCompatActivity) context;
        
        activity.runOnUiThread(() -> {
            try {
                WebView webView = new WebView(context);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setDomStorageEnabled(true);
                webView.setVisibility(View.GONE);
                
                webView.addJavascriptInterface(new Object() {
                    @JavascriptInterface
                    public void processContent(final String html) {
                        activity.runOnUiThread(() -> {
                            webView.destroy();
                            callback.onSuccess(html);
                        });
                    }
                    
                    @JavascriptInterface
                    public void onError(String error) {
                        activity.runOnUiThread(() -> {
                            webView.destroy();
                            callback.onError(error);
                        });
                    }
                }, "AndroidBridge");
                
                webView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String loadedUrl) {
                        // Only extract content from the target URL, not the form page
                        if (loadedUrl.contains(url.substring(url.lastIndexOf("/")))) {
                            webView.loadUrl("javascript:window.AndroidBridge.processContent(document.body.innerText);");
                        }
                    }
                    
                    @Override
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        activity.runOnUiThread(() -> {
                            webView.destroy();
                            callback.onError("WebView error: " + description);
                        });
                    }
                });
                
                // Create HTML form for POST submission
                String htmlForm = createHtmlForm(url, requestBody);
                webView.loadDataWithBaseURL(null, htmlForm, "text/html", "UTF-8", null);
                
            } catch (Exception e) {
                callback.onError("Error creating WebView form: " + e.getMessage());
            }
        });
    }
    
    private static String createHtmlForm(String url, RequestBody requestBody) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head><title>CineStream Request</title></head>\n");
        html.append("<body>\n");
        html.append("<form id='autoForm' method='post' action='").append(url).append("'>\n");
        
        if (requestBody instanceof FormBody) {
            FormBody formBody = (FormBody) requestBody;
            for (int i = 0; i < formBody.size(); i++) {
                String name = formBody.name(i);
                String value = formBody.value(i);
                
                // Escape HTML characters
                name = name.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                value = value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
                
                html.append("<input type='hidden' name='").append(name).append("' value='").append(value).append("'>\n");
            }
        }
        
        html.append("</form>\n");
        html.append("<script>\n");
        html.append("document.getElementById('autoForm').submit();\n");
        html.append("</script>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
}
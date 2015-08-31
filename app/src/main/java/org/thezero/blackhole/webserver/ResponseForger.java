package org.thezero.blackhole.webserver;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.nikkii.embedhttp.impl.HttpStatus;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by thezero on 30/08/15.
 */
public class ResponseForger {
    private static byte[] response;
    private static String contentType;
    private static HttpStatus status;

    public ResponseForger(byte[] r, String ct, HttpStatus s) {
        response=r;
        contentType=ct;
        status=s;
    }

    public ResponseForger(String r, String ct, HttpStatus s) {
        response=r.getBytes();
        contentType=ct;
        status=s;
    }

    public int getStatusCode() {
        return status.getCode();
    }

    public void setStatusCode(HttpStatus s) {
        status=s;
    }

    public HttpEntity forgeResponse(Object mustache){
        Template tmpl = Mustache.compiler().compile(new String(response));
        response=tmpl.execute(mustache).getBytes();

        return this.forgeResponse();
    }

    public HttpEntity forgeResponse(){
        HttpEntity entity = new EntityTemplate(new ContentProducer() {
            public void writeTo(final OutputStream outstream) throws IOException {
                outstream.write(response);
            }
        });

        ((EntityTemplate)entity).setContentType(contentType);
        return entity;
    }
}

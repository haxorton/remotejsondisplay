package org.kazadoom.atlassian.plugin.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.content.render.xhtml.XhtmlException;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.confluence.xhtml.api.MacroDefinition;
import com.atlassian.confluence.xhtml.api.MacroDefinitionHandler;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.net.NonMarshallingRequestFactory;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.util.concurrent.atomic.AtomicReference;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Scanned
public class RemoteJSONDisplayMacro implements Macro {

    private final XhtmlContent xhtmlUtils;
    private final NonMarshallingRequestFactory<Request<?, Response>> requestFactory;

    @Autowired
    public RemoteJSONDisplayMacro(
            @ComponentImport XhtmlContent xhtmlUtils,
            @ComponentImport NonMarshallingRequestFactory<Request<?, Response>> requestFactory) {
        this.xhtmlUtils = xhtmlUtils;
        this.requestFactory = requestFactory;
    }

    @Override
    public String execute(Map<String, String> parameters, String bodyContent, ConversionContext conversionContext) throws MacroExecutionException {
        String url = parameters.get("jsonurl");
        if(null==url) {
            throw new MacroExecutionException("No JSON URL given.");
        }
        UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
        if(!urlValidator.isValid(url)) {
            throw new MacroExecutionException("The given URL \"" + url + "\" is not a valid URL");
        }
        final AtomicReference<String> result = new AtomicReference<>();
        final Request<?, Response> request =  requestFactory.createRequest(Request.MethodType.GET,url);
        try {
            request.execute(response -> {
                result.set(response.getResponseBodyAsString());
            });
        } catch (ResponseException e) {
            throw new MacroExecutionException(e);
        }

//        else if (true) {
//            UrlValidator
//        }
        String body = conversionContext.getEntity().getBodyAsString();
        final List<MacroDefinition> macros = new ArrayList<MacroDefinition>();
        try {
            xhtmlUtils.handleMacroDefinitions(body, conversionContext, new MacroDefinitionHandler() {
                @Override
                public void handle(MacroDefinition macroDefinition) {
                    macros.add(macroDefinition);
                }
            });
        } catch (XhtmlException e) {
            throw new MacroExecutionException(e);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<p>");
        builder.append(result.get());
        builder.append("</p>");
        return builder.toString();
//        if(!macros.isEmpty()) {
//            builder.append("<table width=\"50%\">");
//            builder.append("<tr><th>Macro Name</th><th>Has Body?</th></tr>");
//            for(MacroDefinition def : macros) {
//                builder.append("<tr>");
//                builder.append("<td>").append(def.getName()).append("</td><td>").append(def.hasBody()).append("</td>");
//                builder.append("</tr>");
//            }
//            builder.append("</table>");
//        } else {
//            builder.append("You've built yourself a macro! Nice work.");
//        }
//        builder.append("</p>");
//        return builder.toString();
    }

    @Override
    public BodyType getBodyType() {
        return BodyType.NONE;
    }

    @Override
    public OutputType getOutputType() {
        return OutputType.BLOCK;
    }
}

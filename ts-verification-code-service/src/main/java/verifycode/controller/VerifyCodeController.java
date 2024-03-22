package verifycode.controller;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import verifycode.service.VerifyCodeService;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @author fdse
 */
@RestController
@RequestMapping("/api/v1/verifycode")
public class VerifyCodeController {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyCodeController.class);

    @Autowired
    private MeterRegistry meterRegistry;
    private Counter imageCode_error;
    private Counter verifyCode_error;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-verification-code-service");
        meterRegistry.config().commonTags(tags);
        imageCode_error = Counter.builder("ts.verification-code.imageCode.error").register(meterRegistry);
        verifyCode_error = Counter.builder("ts.verification-code.verifyCode.error").register(meterRegistry);
    }

    @Autowired
    private VerifyCodeService verifyCodeService;

    @GetMapping("/generate")
    @Timed(value = "ts.verification-code.imageCode")
    public void imageCode(@RequestHeader HttpHeaders headers,
                          HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        VerifyCodeController.LOGGER.info("[imageCode][Image code]");
        OutputStream os = response.getOutputStream();
        Map<String, Object> map = verifyCodeService.getImageCode(60, 20, os, request, response, headers);
        String simpleCaptcha = "simpleCaptcha";
        request.getSession().setAttribute(simpleCaptcha, map.get("strEnsure").toString().toLowerCase());
        request.getSession().setAttribute("codeTime", System.currentTimeMillis());
        try {
            ImageIO.write((BufferedImage) map.get("image"), "JPEG", os);
        } catch (IOException e) {
            //error
            String error = "Can't generate verification code";
            os.write(error.getBytes());
            imageCode_error.increment();
        }
    }

    @GetMapping(value = "/verify/{verifyCode}")
    @Timed(value = "ts.verification-code.verifyCode")
    public boolean verifyCode(@PathVariable String verifyCode, HttpServletRequest request,
                              HttpServletResponse response, @RequestHeader HttpHeaders headers) {
        LOGGER.info("[verifyCode][receivedCode: {}]", verifyCode);

        boolean result = verifyCodeService.verifyCode(request, response, verifyCode, headers);
        LOGGER.info("[verifyCode][verify result: {}]", result);

        if (!result) {
            verifyCode_error.increment();
        }
        return true;
    }
}

package pillihuaman.com.pe.neuroIA.Controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pillihuaman.com.pe.lib.common.MyJsonWebToken;
import pillihuaman.com.pe.lib.common.RespBase;
import pillihuaman.com.pe.neuroIA.Help.Constante;
import pillihuaman.com.pe.neuroIA.JwtService;
import pillihuaman.com.pe.neuroIA.Service.Implement.AwsAccountInfoService;
import pillihuaman.com.pe.neuroIA.dto.AwsResourcesSummary;

@RestController
@RequestMapping(Constante.BASE_ENDPOINT + Constante.ENDPOINT + "/awsService")
public class AwsController {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AwsAccountInfoService awsAccountInfoService;

    @GetMapping(value = "/ec2/instances", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RespBase<String>> getEc2Instances() {
        RespBase<String> response = new RespBase<>();
        MyJsonWebToken token = jwtService.parseTokenToMyJsonWebToken(httpServletRequest.getHeader("Authorization"));
        String data = awsAccountInfoService.describeInstances();
        return ResponseEntity.ok(response.ok(data));

    }

    //RespBase<RespIa> result
    @GetMapping(value = "/billing/monthly", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RespBase<String>> getMonthlyBilling() {
        RespBase<String> response = new RespBase<>();

        MyJsonWebToken token = jwtService.parseTokenToMyJsonWebToken(httpServletRequest.getHeader("Authorization"));
        String data = awsAccountInfoService.getBillingLastMonth();
        return ResponseEntity.ok(response.ok(data));

    }


    @GetMapping(value = "/resources/structured", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RespBase<AwsResourcesSummary>> getAllResourcesJson() {
        RespBase<AwsResourcesSummary> response = new RespBase<>();

        MyJsonWebToken token = jwtService.parseTokenToMyJsonWebToken(httpServletRequest.getHeader("Authorization"));
        AwsResourcesSummary data = awsAccountInfoService.getAllAwsResourcesSummaryAsObject();
        response.setData(data);
        return ResponseEntity.ok(response);

    }


}

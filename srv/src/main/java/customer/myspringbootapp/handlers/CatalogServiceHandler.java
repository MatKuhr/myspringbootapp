package customer.myspringbootapp.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.sap.cds.services.cds.CdsReadEventContext;
import com.sap.cds.services.cds.CdsService;
import com.sap.cds.services.handler.EventHandler;
import com.sap.cds.services.handler.annotations.After;
import com.sap.cds.services.handler.annotations.On;
import com.sap.cds.services.handler.annotations.ServiceName;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.datamodel.odata.client.exception.ODataException;
import com.sap.cloud.sdk.datamodel.odata.client.exception.ODataResponseException;
import com.sap.cloud.sdk.datamodel.odata.client.exception.ODataServiceErrorException;

import cds.gen.catalogservice.CatalogService_;
import cds.gen.gwsample_basic.BusinessPartnerSet;
import cds.gen.catalogservice.Books;

import com.vdm.services.DefaultGwSampleBasicService;
import com.vdm.namespaces.gwsamplebasic.BusinessPartner;

@Component
@ServiceName(CatalogService_.CDS_NAME)
public class CatalogServiceHandler implements EventHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @After(event = CdsService.EVENT_READ, entity = "CatalogService.Books")
    public void discountBooks(Stream<Books> books) {
        books.filter(b -> b.getTitle() != null && b.getStock() != null).filter(b -> b.getStock() > 200)
                .forEach(b -> b.setTitle(b.getTitle() + " (discounted)"));
    }

    @On(event = CdsService.EVENT_READ, entity = "CatalogService.BusinessPartner")
    public void getBusinessPartners(CdsReadEventContext context) throws ODataException {
        log.info("Entering " + getClass().getSimpleName() + ":getBusinessPartners");
        System.out.println("Entering " + getClass().getSimpleName() + ":getBusinessPartners");

        // Get name of destination for ECC
        final String DESTINATION_HEADER_KEY = "es5";

        final Map<Object, Map<String, Object>> result = new HashMap<>();

        HttpDestination dest = DestinationAccessor.getDestination(DESTINATION_HEADER_KEY).asHttp();
        final List<BusinessPartner> businessPartners;

        try {
            businessPartners = new DefaultGwSampleBasicService().getAllBusinessPartner().top(5).executeRequest(dest);
        } catch (ODataServiceErrorException e) {
            log.error("The service responded with status code {} and an OData error: {}", e.getHttpCode(), e.getOdataError());
            log.error("Initial request was: {}", e.getRequest());
            throw e;
        } catch (ODataResponseException e) {
            log.error("The service responded with status code {}", e.getHttpCode());
            log.error("Initial request was: {}", e.getRequest());
            throw e;
        }

        final List<cds.gen.catalogservice.BusinessPartner> capBusinessPartners = new ArrayList<>();

        for (final BusinessPartner bp : businessPartners) {
            final cds.gen.catalogservice.BusinessPartner capBusinessPartner = com.sap.cds.Struct
                    .create(cds.gen.catalogservice.BusinessPartner.class);

            System.out.println(bp.getBusinessPartnerID());

            capBusinessPartner.setBusinessPartnerID(bp.getBusinessPartnerID());
            capBusinessPartner.setCompanyName(bp.getCompany());
            capBusinessPartner.setEmailAddress(bp.getEMail());
            capBusinessPartner.setPhoneNumber(bp.getPhoneNo());

            capBusinessPartners.add(capBusinessPartner);
        }

        capBusinessPartners.forEach(capBusinessPartner -> {
            result.put(capBusinessPartner.getBusinessPartnerID(), capBusinessPartner);
        });

        context.setResult(result.values());

    }

}
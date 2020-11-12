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

import cds.gen.catalogservice.CatalogService_;
import cds.gen.gwsample_basic.BusinessPartnerSet;
import cds.gen.catalogservice.Books;

import com.vdm.services.DefaultGWSAMPLEBASICService;
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
    public void getBusinessPartners(CdsReadEventContext context) {
        log.info("Entering " + getClass().getSimpleName() + ":getBusinessPartners");
        System.out.println("Entering " + getClass().getSimpleName() + ":getBusinessPartners");

        // Get name of destination for ECC
        final String DESTINATION_HEADER_KEY = "es5";

        final Map<Object, Map<String, Object>> result = new HashMap<>();

        HttpDestination dest = DestinationAccessor.getDestination(DESTINATION_HEADER_KEY).asHttp();

        final List<BusinessPartner> businessPartners = new DefaultGWSAMPLEBASICService().getAllBusinessPartner().top(10)
                .executeRequest(dest);

        final List<cds.gen.catalogservice.BusinessPartner> capBusinessPartners = new ArrayList<>();

        for( final BusinessPartner bp : businessPartners ) {
            final cds.gen.catalogservice.BusinessPartner capBusinessPartner = com.sap.cds.Struct.create(cds.gen.catalogservice.BusinessPartner.class);

            //capBusinessPartner.setBusinessPartnerID(bp.);
            //capBusinessPartner.setFirstName(s4BusinessPartner.getFirstName());
            //capBusinessPartner.setSurname(s4BusinessPartner.getLastName());
            //capBusinessPartner.setId(s4BusinessPartner.getBusinessPartner());
            //capBusinessPartner.setSourceDestination(destinationName);

            capBusinessPartners.add(capBusinessPartner);
        }

        capBusinessPartners.forEach(capBusinessPartner -> {
            result.put(capBusinessPartner.getBusinessPartnerID(), capBusinessPartner);
        });

        context.setResult(result.values());

    }

}
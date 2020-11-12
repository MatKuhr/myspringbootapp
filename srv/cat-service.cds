using my.bookshop as my from '../db/data-model';
using { GWSAMPLE_BASIC as external } from './external/GWSAMPLE_BASIC.csn';

service CatalogService {
    entity Books as projection on my.Books;
    entity BusinessPartner as projection on external.BusinessPartnerSet {
        BusinessPartnerID,CompanyName,EmailAddress,PhoneNumber
    };
}
package br.com.ederoliv.goeat_api.dto.partner;
import br.com.ederoliv.goeat_api.dto.address.AddressRequestDTO;
import java.util.List;


public record PartnerDetailsData(

        String name,
        String phone,
        String email,

        AddressRequestDTO address,

        List<Long> categoryIds
) {}
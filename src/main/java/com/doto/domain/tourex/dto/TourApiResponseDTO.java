package com.doto.domain.tourex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * 한국관광공사 TourAPI 응답을 위한 외부 API 전용 DTO
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TourApiResponseDTO(
        Response response
) {

    public List<TourContentDTO> itemsOrEmpty() {
        if (response == null || response.body() == null || response.body().items() == null
                || response.body().items().item() == null) {
            return List.of();
        }
        return response.body().items().item();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Response(
            Header header,
            Body body
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Header(
            String resultCode,
            String resultMsg
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Body(
            @JsonDeserialize(using = ItemsDeserializer.class)
            Items items,
            Integer numOfRows,
            Integer pageNo,
            Integer totalCount
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Items(
            List<TourContentDTO> item
    ) {
    }

    // TourAPI가 조회 결과가 없을 때 items를 빈 문자열 또는 배열로 내려주는 경우를 빈 목록으로 처리
    public static class ItemsDeserializer extends StdDeserializer<Items> {

        public ItemsDeserializer() {
            super(Items.class);
        }

        @Override
        public Items deserialize(JsonParser parser, tools.jackson.databind.DeserializationContext context)
                throws tools.jackson.core.JacksonException {
            JsonNode itemsNode = context.readTree(parser);
            if (itemsNode == null || itemsNode.isNull() || itemsNode.isArray()
                    || (itemsNode.isTextual() && itemsNode.asText().isBlank())) {
                return new Items(List.of());
            }

            JsonNode itemNode = itemsNode.get("item");
            if (itemNode == null || itemNode.isNull()) {
                return new Items(List.of());
            }
            if (itemNode.isArray()) {
                return new Items(Arrays.asList(context.readTreeAsValue(itemNode, TourContentDTO[].class)));
            }
            return new Items(List.of(context.readTreeAsValue(itemNode, TourContentDTO.class)));
        }
    }

    //관광지와 축제 목록·상세 조회에서 공통으로 사용하는 콘텐츠 정보

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TourContentDTO(
            @JsonProperty("contentid") Long contentId,
            @JsonProperty("contenttypeid") Integer contentTypeId,
            String title,
            String homepage,
            String addr1,
            String addr2,
            String tel,
            String firstimage,
            String firstimage2,
            String mapx,
            String mapy,
            String modifiedtime,
            @JsonProperty("dist") BigDecimal distance,
            @JsonProperty("lDongRegnCd") String legalDongRegionCode,
            @JsonProperty("lDongSignguCd") String legalDongSigunguCode,
            @JsonProperty("lclsSystm1") String lclsSystem1,
            @JsonProperty("lclsSystm2") String lclsSystem2,
            @JsonProperty("lclsSystm3") String lclsSystem3,
            String overview,
            String eventstartdate,
            String eventenddate,
            String eventplace,
            String playtime,
            String usetimefestival,
            @JsonProperty("festivaltype") String festivalType
    ) {
    }
}

package ru.zz.jackson;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({"ok", "result"})

public class IncomeMessage {
  //  @JsonProperty("ok")
    private Boolean ok;
 //   @JsonProperty("result")
    private List<Result> result = new ArrayList<>();

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("ok")
    public Boolean getOk() {
        return ok;
    }
    @JsonProperty("ok")
    public void setOk(Boolean ok) {
        this.ok = ok;
    }

    @JsonProperty("result")
    public List<Result> getResult() {
        return result;
    }
    @JsonProperty("result")
    public void setResult(List<Result> result) {
        this.result = result;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }
    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
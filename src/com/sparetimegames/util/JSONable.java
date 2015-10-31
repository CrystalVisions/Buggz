package com.sparetimegames.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: dianeloux
 * Date: 8/1/12
 * Time: 6:41 AM
 * Copyright(c) Diane Loux 2012
 * and interface for JSON-able classes
 */
public interface JSONable
{
    /**
     * Must return a JSON-compliant representation of this object,
     * in the form of a JSONObject.
     *
     * @return The JSONObject representation.
     * @throws JSONException if any of the underlying JSONObject members throws.
     *                                Implementations may throw other unchecked exceptions.
     */
    public JSONObject toJSONObject();

    /**
     * Must populate this object from the given JSON source.
     *
     * @param src The source JSON data.
     * @throws JSONException If any JSONObject members throw. Implementations
     * may optionally throw if the input object violates the expected structure.
     */
    public boolean fromJSONObject(final JSONObject src);


}

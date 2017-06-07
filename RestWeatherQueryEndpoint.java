package com.weather;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import com.weather.loader.AirportDataLoader;
import com.google.gson.Gson;

/**
 * The Weather App REST endpoint allows clients to query, update and check
 * health stats. Currently, all data is held in memory. The end point deploys to
 * a single container
 *
 * @author Unni Vemanchery Mana
 */
@Path("/query")
public class RestWeatherQueryEndpoint implements WeatherQueryEndpoint
{

    public final static Logger LOGGER = Logger.getLogger("WeatherQuery");

    /** earth radius in KM */
    public static final double R = 6372.8;

    /** shared gson json to object factory */
    public static final Gson gson = new Gson();

    /** all known airports */
    // protected static List<AirportData> airportData = new ArrayList<>(); //
    // there is every chance of thread issue

    protected static CopyOnWriteArrayList<AirportData> airportData = new CopyOnWriteArrayList<>();

    /**
     * atmospheric information for each airport, idx corresponds with
     * airportData
     */
    // protected static List<AtmosphericInformation> atmosphericInformation =
    // new LinkedList<>();

    protected static List<AtmosphericInformation> atmosphericInformation = Collections
            .synchronizedList(new LinkedList<>());

    /**
     * Internal performance counter to better understand most requested
     * information, this map can be improved but for now provides the basis for
     * future performance optimizations. Due to the state less deployment
     * architecture we don't want to write this to disk, but will pull it off
     * using a REST request and aggregate with other performance metrics
     * {@link #ping()}
     */
    // public static Map<AirportData, Integer> requestFrequency = new
    // HashMap<AirportData, Integer>();
    public static Map<AirportData, Integer> requestFrequency = new ConcurrentHashMap<AirportData, Integer>();

    // public static Map<Double, Integer> radiusFreq = new HashMap<Double,
    // Integer>();
    public static Map<Double, Integer> radiusFreq = new ConcurrentHashMap<Double, Integer>();

    /** Airport data loader */
    private static final AirportDataLoader dataLoader = new AirportDataLoader();

    static
    {
        init();
    }

    /**
     * Retrieve service health including total size of valid data points and
     * request frequency information.
     *
     * @return health status for the service as a string
     */
    @Override
    public String ping()
    {
        Map<String, Object> retval = new HashMap<>();

        int datasize = 0;
        for (AtmosphericInformation ai : atmosphericInformation)
        {
            // we only count recent readings
            if (ai.getCloudCover() != null || ai.getHumidity() != null || ai.getPressure() != null
                    || ai.getPrecipitation() != null || ai.getTemperature() != null || ai.getWind() != null)
            {
                // updated in the last day
                if (ai.getLastUpdateTime() > System.currentTimeMillis() - 86400000)
                {
                    datasize++;
                }
            }
        }
        retval.put("datasize", datasize);

        Map<String, Double> freq = new HashMap<>();
        // fraction of queries
        for (AirportData data : airportData)
        {

            double frac = (double) requestFrequency.getOrDefault(data, 0) / requestFrequency.size();

            //////// to save from NaN. GSON does not serialize if type == NaN
            if (Double.isNaN(frac))
                frac = 0.0;

            freq.put(data.getIata(), frac);
        }
        retval.put("iata_freq", freq);

        int m = radiusFreq.keySet().stream().max(Double::compare).orElse(1000.0).intValue() + 1;

        int[] hist = new int[m];
        for (Map.Entry<Double, Integer> e : radiusFreq.entrySet())
        {
            int i = e.getKey().intValue() % 10;
            hist[i] += e.getValue();
        }
        retval.put("radius_freq", hist);
        return gson.toJson(retval);
    }

    /**
     * Given a query in json format {'iata': CODE, 'radius': km} extracts the
     * requested airport information and return a list of matching atmosphere
     * information.
     *
     * @param iata
     *            the iataCode
     * @param radiusString
     *            the radius in km
     *
     * @return a list of atmospheric information
     */
    @Override
    public Response weather(String iata, String radiusString)
    {
        double radius = radiusString == null || radiusString.trim().isEmpty() ? 0 : Double.valueOf(radiusString);

        AirportData ad = findAirportData(iata);
        updateRequestFrequency(iata, radius, ad);
        List<AtmosphericInformation> retval = new ArrayList<>();

        if (radius == 0)
        {
            int idx = getAirportDataIdx(iata);
            if (idx == -1)
            {
                AtmosphericInformation ai = new AtmosphericInformation();
                retval.add(ai);
            }
            else
                retval.add(atmosphericInformation.get(idx));
        }
        else
        {

            if (ad == null)
            {
                retval.add(new AtmosphericInformation());
            }
            else
                for (int i = 0; i < airportData.size(); i++)
                {
                    double cd = calculateDistance(ad, airportData.get(i));
                    if (cd <= radius)
                    {
                        AtmosphericInformation ai = atmosphericInformation.get(i);
                        if (ai.getCloudCover() != null || ai.getHumidity() != null || ai.getPrecipitation() != null
                                || ai.getPressure() != null || ai.getTemperature() != null || ai.getWind() != null)
                        {
                            AtmosphericInformationDecorator aid = (AtmosphericInformationDecorator) ai;
                            aid.setIataCode(airportData.get(i).getIata());
                            ai = aid;
                            retval.add(ai);
                        }
                    }
                }

        }

        return Response.status(Response.Status.OK).entity(gson.toJson(retval)).build();
    }

    /**
     * Records information about how often requests are made
     *
     * @param iata
     *            an iata code
     * @param radius
     *            query radius
     */
    public void updateRequestFrequency(String iata, Double radius, AirportData airportData)
    {
        if (airportData == null)
            return;
        requestFrequency.put(airportData, requestFrequency.getOrDefault(airportData, 0) + 1);
        radiusFreq.put(radius, radiusFreq.getOrDefault(radius, 0));
    }

    /**
     * Given an iataCode find the airport data
     *
     * @param iataCode
     *            as a string
     * @return airport data or null if not found
     */
    public static AirportData findAirportData(String iataCode)
    {
        return airportData.stream().filter(ap -> ap.getIata().equalsIgnoreCase(iataCode)).findFirst().orElse(null);
    }

    /**
     * Given an iataCode find the airport data
     *
     * @param iataCode
     *            as a string
     * @return airport data or null if not found
     */
    public static int getAirportDataIdx(String iataCode)
    {
        AirportData ad = findAirportData(iataCode);
        return airportData.indexOf(ad);
    }

    /**
     * Haversine distance between two airports.
     *
     * @param ad1
     *            airport 1
     * @param ad2
     *            airport 2
     * @return the distance in KM
     */
    public double calculateDistance(AirportData ad1, AirportData ad2)
    {
        double deltaLat = Math.toRadians(ad2.latitude - ad1.latitude);
        double deltaLon = Math.toRadians(ad2.longitude - ad1.longitude);
        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                   + Math.pow(Math.sin(deltaLon / 2), 2) * Math.cos(ad1.latitude) * Math.cos(ad2.latitude);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    /**
     * loading airport data
     *
     * @param
     * @return
     */
    public static void init()
    {
        dataLoader.load();
    }

}

package com.smidur.aventon.utilities;

import android.support.annotation.NonNull;

/**
 * Created by marqueg on 11/16/15.
 */
public class GoogleMapRouteRequestBuilder {
    /**
     * Takes list of locations to build the route calculation request
     *
     * @param locationPoints double array of arrays,
     *                            expects a minimum of 2 locations (origin and destination)
     *                            need to be sorted as well already.
     *                            first array has every delivery location for route calculation.
     *                            second nested array has latitude and longitude
     *
     * @return request to send to Google Api with properly formatted "way points".
     */
    public static String buildRequest(@NonNull double[][] locationPoints) {
        StringBuilder deliveriesBuilder = new StringBuilder();
        if(locationPoints.length <2)return null;//dont even bother
        else {//size is at least 2 is good
            deliveriesBuilder.append(
                    String.format("origin=%.05f,%.05f&destination=%.05f,%.05f",
                            locationPoints[0][0],locationPoints[0][1],//always use first location
                            //always use last location for destination
                            locationPoints[locationPoints.length-1][0],locationPoints[locationPoints.length-1][1])
            );

        }
        //more than two locations means we need to add  way points
        if(locationPoints.length>2) {
            deliveriesBuilder.append("&waypoints=");
            String multipleWaypointsSeparator = "";
            //origin and destination are not waypoints
            for(int i = 1; i < locationPoints.length-1;i++) {
                //we still have another waypoint to process
                if(i != locationPoints.length-1) {
                    multipleWaypointsSeparator = "|";
                } else {
                    multipleWaypointsSeparator = "";
                }
                deliveriesBuilder.append(String.format("%.05f,%.05f"
                        +multipleWaypointsSeparator
                        ,locationPoints[i][0],locationPoints[i][1]));
            }
        }
        return deliveriesBuilder.toString();
    }
}

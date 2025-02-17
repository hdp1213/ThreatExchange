package com.facebook.threatexchange;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.NumberFormatException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * HTTP-wrapper methods
 * See also https://developers.facebook.com/docs/threat-exchange
 */
class Net {
  private static String APP_TOKEN = null;

  /**
   * Gets the ThreatExchange app token from an environment variable.
   * Feel free to replace the app-token discovery method here with whatever
   * is most convenient for your project. However, be aware that app tokens
   * are like passwords and shouldn't be stored in the open.
   */
  public static void setAppToken(String appTokenEnvName) {
    String value = System.getenv().get(appTokenEnvName);
    if (value == null) {
      System.out.printf("Must set %s environment variable in format %s.\n",
        appTokenEnvName, "999999999999999|xxxx-xxxxxxxxx-xxxxxxxxxxxx");
      System.exit(1);
    }
    APP_TOKEN = value;
  }

  /**
   * Looks up the internal ID for a given tag.
   */
  public static String getTagIDFromName(String tagName, boolean showURLs) {
    String url = Constants.TE_BASE_URL
      + "/threat_tags"
      + "/?access_token=" + APP_TOKEN
      + "&text=" + URLEncoder.encode(tagName); // since user-supplied string
    if (showURLs) {
      System.out.println("URL:");
      System.out.println(url);
    }
    try (InputStream response = new URL(url).openConnection().getInputStream()) {

      // The lookup will get everything that has this as a prefix.
      // So we need to filter the results. This loop also handles the
      // case when the results array is empty.

      // Example: when querying for "media_type_video", we want the 2nd one:
      // { "data": [
      //   { "id": "9999338563303771", "text": "media_type_video_long_hash" },
      //   { "id": "9999474908560728", "text": "media_type_video" },
      //   { "id": "9889872714202918", "text": "media_type_video_hash_long" }
      //   ], ...
      // }

      JSONObject object = (JSONObject) new JSONParser().parse(new InputStreamReader(response));
      JSONArray idAndTextArray = (JSONArray) object.get("data");

      for (Object idAndTextObj : idAndTextArray) {
        JSONObject idAndText = (JSONObject)idAndTextObj;
        String id = (String)idAndText.get("id");
        String text = (String)idAndText.get("text");
        if (text != null && text.equals(tagName)) {
          return id;
        }
      }
      return null;
    } catch (Exception e) {
      e.printStackTrace(System.err);
      System.exit(1);
    }
    return null;
  }

  /**
   * Looks up all descriptors with a given tag. Invokes a specified callback on
   * each page of IDs.
   */
  public static void getHashIDsByTagID(
    String tagID,
    boolean verbose,
    boolean showURLs,
    HashFilterer hashFilterer,
    String since, // maybe null
    String until, // maybe null
    int pageSize,
    boolean printHashString,
    IDProcessor idProcessor
  ) {
    String pageLimit = Integer.toString(pageSize);
    String startURL = Constants.TE_BASE_URL
      + "/" + tagID + "/tagged_objects"
      + "/?access_token=" + APP_TOKEN
      + "&limit=" + pageLimit;
    if (since != null) {
      startURL += "&tagged_since=" + since;
    }
    if (until != null) {
      startURL += "&tagged_until=" + until;
    }

    String nextURL = startURL;

    int pageIndex = 0;
    do {
      if (showURLs) {
        System.out.println("URL:");
        System.out.println(nextURL);
      }
      try (InputStream response = new URL(nextURL).openConnection().getInputStream()) {

        // Format we're parsing:
        // {
        //   "data": [
        //     {
        //       "id": "9915337796604770",
        //       "type": "THREAT_DESCRIPTOR",
        //       "name": "7ef5...aa97"
        //     }
        //     ...
        //   ],
        //   "paging": {
        //     "cursors": {
        //       "before": "XYZIU...NjQ0h3Unh3",
        //       "after": "XYZIUk...FXNzVNd1Jn"
        //     },
        //     "next": "https://graph.facebook.com/v3.1/9999338387644295/tagged_objects?access_token=..."
        //   }
        // }

        JSONObject object = (JSONObject) new JSONParser().parse(new InputStreamReader(response));
        JSONArray data = (JSONArray) object.get("data");
        JSONObject paging = (JSONObject) object.get("paging");
        if (paging == null) {
          nextURL = null;
        } else {
          nextURL = (String) paging.get("next");
        }

        int numItems = data.size();

        List<String> hashIDs = new ArrayList<String>();
        for (int i = 0; i < numItems; i++) {
          JSONObject item = (JSONObject) data.get(i);

          String itemID = (String) item.get("id");
          String itemType = (String) item.get("type");
          String itemText = (String) item.get("name");
          if (!itemType.equals(Constants.THREAT_DESCRIPTOR)) {
            continue;
          }
          if (!hashFilterer.accept(itemText)) {
            continue;
          }

          if (verbose) {
            SimpleJSONWriter w = new SimpleJSONWriter();
            w.add("id", itemID);
            w.add("type", itemType);
            if (printHashString) {
              w.add("hash", itemText);
            }

            System.out.println(w.format());
            System.out.flush();
          }
          hashIDs.add(itemID);
        }

        if (verbose) {
          SimpleJSONWriter w = new SimpleJSONWriter();
          w.add("page_index", pageIndex);
          w.add("num_items_pre_filter", numItems);
          w.add("num_items_post_filter", hashIDs.size());
          System.out.println(w.format());
          System.out.flush();
        }

        idProcessor.processIDs(hashIDs);

        pageIndex++;
      } catch (Exception e) {
        e.printStackTrace(System.err);
        System.exit(1);
      }
    } while (nextURL != null);
  }

  /**
   * Looks up all metadata for given ID.
   */
  public static List<SharedHash> getInfoForIDs(
    List<String> hashIDs,
    boolean verbose,
    boolean showURLs,
    boolean printHashString
  ) {
    // Check well-formattedness of hash IDs (which may have come from
    // arbitrary data on stdin).
    for(String hashID : hashIDs) {
      try {
        Long.valueOf(hashID);
      } catch (NumberFormatException e) {
        System.err.printf("Malformed hash ID \"%s\"\n", hashID);
        System.exit(1);
      }
    }

    String url = Constants.TE_BASE_URL
      + "/?access_token=" + APP_TOKEN
      + "&ids=%5B" + String.join(",", hashIDs) + "%5D"
      + "&fields=raw_indicator,type,added_on,confidence,owner,review_status,severity,share_level,tags";
    if (showURLs) {
      System.out.println("URL:");
      System.out.println(url);
    }

    List<SharedHash> sharedHashes = new ArrayList<SharedHash>();
    try (InputStream response = new URL(url).openConnection().getInputStream()) {
      // {
      //    "990927953l366387": {
      //       "raw_indicator": "87f4b261064696075fffceee39471952",
      //       "type": "HASH_MD5",
      //       "added_on": "2018-03-21T18:47:23+0000",
      //       "confidence": 100,
      //       "owner": {
      //          "id": "788842735455502",
      //          "email": "contactemail\u0040companyname.com",
      //          "name": "Name of App"
      //       },
      //       "review_status": "REVIEWED_AUTOMATICALLY",
      //       "severity": "WARNING",
      //       "share_level": "AMBER",
      //       "tags": {
      //          "data": [
      //             {
      //                "id": "8995447960580728",
      //                "text": "media_type_video"
      //             },
      //             {
      //                "id": "6000177b99449380",
      //                "text": "media_priority_test"
      //             }
      //          ]
      //       },
      //       "id": "4019972332766623"
      //    },
      //    ...
      //  }

      JSONObject outer = (JSONObject) new JSONParser().parse(new InputStreamReader(response));

      for (Iterator iterator = outer.keySet().iterator(); iterator.hasNext();) {
        String key = (String) iterator.next();
        JSONObject item = (JSONObject) outer.get(key);

        if (verbose) {
          System.out.println(item.toString());
        }

        JSONObject owner = (JSONObject)item.get("owner");
        JSONObject tags = (JSONObject)item.get("tags");
        JSONArray tag_data = (JSONArray)tags.get("data");
        int n = tag_data.size();
        String mediaType = "not_found";
        String mediaPriority = "not_found";
        int numMediaType = 0;
        int numMediaPriority = 0;
        for (int j = 0; j < n; j++) {
          JSONObject tag = (JSONObject) tag_data.get(j);
          String tag_text = (String)tag.get("text");
          if (tag_text.startsWith(Constants.TAG_PREFIX_MEDIA_TYPE)) {
            mediaType = tag_text.replace(Constants.TAG_PREFIX_MEDIA_TYPE, "").toUpperCase();
            numMediaType++;
          } else if (tag_text.startsWith(Constants.TAG_PREFIX_MEDIA_PRIORITY)) {
            mediaPriority = tag_text.replace(Constants.TAG_PREFIX_MEDIA_PRIORITY, "").toUpperCase();
            numMediaPriority++;
          }
        }

        if (verbose) {
          if (numMediaType != 1 || numMediaPriority != 1) {
            SimpleJSONWriter w = new SimpleJSONWriter();
            w.add("hash_id", (String)item.get("id"));
            w.add("num_media_type", numMediaType);
            w.add("num_media_priority", numMediaPriority);
            System.out.println(w.format());
          }
        }

        SharedHash sharedHash = new SharedHash(
          (String)item.get("id"),
          (String)item.get("raw_indicator"),
          (String)item.get("type"),
          (String)item.get("added_on"),
          Long.toString((Long)item.get("confidence")),
          (String)owner.get("id"),
          (String)owner.get("email"),
          (String)owner.get("name"),
          mediaType,
          mediaPriority);

        sharedHashes.add(sharedHash);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
      System.exit(1);
    }
    return sharedHashes;
  }

  /**
   * Looks up all descriptors with a given tag (or tag-prefix), optional
   * hash-type filter, and TE 'since' parameter. Warning: often infinite-loopy!
   * There are many queries for which the 'next' paging parameter will remain
   * non-null on every next-page request.
   */
  public static List<SharedHash> getIncremental(
    String tagName,
    String hashType,
    String since,
    int pageSize,
    boolean verbose,
    boolean showURLs
  ) {
    List<SharedHash> sharedHashes = new ArrayList<SharedHash>();

    String pageLimit = Integer.toString(pageSize);
    String startURL = Constants.TE_BASE_URL
      + "/threat_descriptors"
      + "/?access_token=" + APP_TOKEN
      + "&fields=raw_indicator,type,added_on,confidence,owner,review_status,severity,share_level,tags"
      + "&limit=" + pageLimit
      + "&tags=" + tagName
      + "&since=" + since;
    if (hashType != null) {
      startURL = startURL + "&type=" + hashType;
    }

    String nextURL = startURL;

    int pageIndex = 0;
    do {
      if (showURLs) {
        System.out.println("URL:");
        System.out.println(nextURL);
      }
      try (InputStream response = new URL(nextURL).openConnection().getInputStream()) {

        // {
        //    "data": [
        //     {
        //        "added_on": "2018-02-15T10:01:38+0000",
        //        "confidence": 50,
        //        "description": "Description goes here",
        //        "id": "9998888887828886",
        //        "indicator": {
        //           "id": "8858889164495553",
        //           "indicator": "0096f7ffffb07f385630008f495b59ff",
        //           "type": "HASH_MD5"
        //        },
        //        "last_updated": "2018-02-15T10:01:39+0000",
        //        "owner": {
        //           "id": "9977777020662433",
        //           "email": "username\u0040companyname.com",
        //           "name": "Name of App"
        //        },
        //        "precision": "UNKNOWN",
        //        "privacy_type": "HAS_PRIVACY_GROUP",
        //        "raw_indicator": "0096f7ffffb07f385630008f495b59ff",
        //        "review_status": "REVIEWED_MANUALLY",
        //        "severity": "WARNING",
        //        "share_level": "AMBER",
        //        "status": "MALICIOUS",
        //        "type": "HASH_MD5"
        //     },
        //    "paging": {
        //       "cursors": {
        //          "before": "MAZDZD",
        //          "after": "MQZDZD"
        //       }
        //    }
        // }

        JSONObject object = (JSONObject) new JSONParser().parse(new InputStreamReader(response));
        JSONArray data = (JSONArray) object.get("data");
        JSONObject paging = (JSONObject) object.get("paging");
        if (paging == null) {
          nextURL = null;
        } else {
          nextURL = (String) paging.get("next");
        }

        int numItems = data.size();
        if (verbose) {
          SimpleJSONWriter w = new SimpleJSONWriter();
          w.add("page_index", pageIndex);
          w.add("num_items", numItems);
          System.out.println(w.format());
          System.out.flush();
        }

        for (int i = 0; i < numItems; i++) {
          JSONObject item = (JSONObject) data.get(i);

          String itemID = (String) item.get("id");
          String itemType = (String) item.get("type");
          String itemText = (String) item.get("raw_indicator");

          if (verbose) {
            SimpleJSONWriter w = new SimpleJSONWriter();
            w.add("id", itemID);
            w.add("type", itemType);
            w.add("hash", itemText);
            System.out.println(w.format());
            System.out.flush();
          }

          JSONObject owner = (JSONObject)item.get("owner");

          JSONObject tags = (JSONObject)item.get("tags");
          JSONArray tag_data = (JSONArray)tags.get("data");
          int n = tag_data.size();
          String mediaType = "not_found";
          String mediaPriority = "not_found";
          int numMediaType = 0;
          int numMediaPriority = 0;
          for (int j = 0; j < n; j++) {
            JSONObject tag = (JSONObject) tag_data.get(j);
            String tag_text = (String)tag.get("text");
            if (tag_text.startsWith(Constants.TAG_PREFIX_MEDIA_TYPE)) {
              mediaType = tag_text.replace(Constants.TAG_PREFIX_MEDIA_TYPE, "").toUpperCase();
              numMediaType++;
            } else if (tag_text.startsWith(Constants.TAG_PREFIX_MEDIA_PRIORITY)) {
              mediaPriority = tag_text.replace(Constants.TAG_PREFIX_MEDIA_PRIORITY, "").toUpperCase();
              numMediaPriority++;
            }
          }

          SharedHash sharedHash = new SharedHash(
            itemID,
            itemText,
            itemType,
            (String)item.get("added_on"),
            Long.toString((Long)item.get("confidence")),
            (String)owner.get("id"),
            (String)owner.get("email"),
            (String)owner.get("name"),
            mediaType,
            mediaPriority);

        }
        pageIndex++;
      } catch (Exception e) {
        e.printStackTrace(System.err);
        System.exit(1);
      }
    } while (nextURL != null);

    return sharedHashes;
  }

  /**
   * Does a single POST to the threat_descriptors endpoint.  See also
   * https://developers.facebook.com/docs/threat-exchange/reference/submitting
   */
  public static void postThreatDescriptor(
    DescriptorPostParameters params,
    boolean showURLs,
    boolean dryRun
  ) {

    String urlString = Constants.TE_BASE_URL
      + "/threat_descriptors"
      + "/?access_token=" + APP_TOKEN;

    if (showURLs) {
      System.out.println();
      System.out.println("URL:");
      System.out.println(urlString);
    }

    URL url = null;
    try {
      url = new URL(urlString);
    } catch (MalformedURLException e) {
      System.err.println("A malformed URL was constructed:");
      System.err.println(urlString);
      System.exit(1);
    }

    HttpURLConnection connection = null;
    try {
      connection = (HttpURLConnection) url.openConnection();
    } catch (IOException e) {
      System.err.println("A malformed URL was constructed:");
      System.err.println(urlString);
      System.exit(1);
    }
    connection.setDoOutput(true); // since there is a POST body

    connection.setInstanceFollowRedirects(false);
    try {
      connection.setRequestMethod("POST");
    } catch (ProtocolException e) {
      System.err.println("Unable to set request method to POST.");
      System.err.println(urlString);
      System.exit(1);
    }
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    connection.setRequestProperty("charset", "utf-8");

    String postDataString = params.getPostDataString();
    if (showURLs) {
      System.out.println();
      System.out.println("POST DATA:");
      System.out.println(postDataString);
    }

    byte[] postDataBytes = Utils.getBytesUTF8(postDataString);
    connection.setRequestProperty("Content-Length", Integer.toString(postDataBytes.length));

    if (dryRun) {
      System.out.println("Not doing POST since --dry-run.");
    } else {
      try {
        connection.getOutputStream().write(postDataBytes);
      } catch (IOException e) {
        System.err.println();
        System.err.printf("POST failure.\n");
        System.err.printf("\n");
        e.printStackTrace(System.err);
        System.exit(1);
      }

      try (InputStream response = connection.getInputStream()) {

        JSONObject object = (JSONObject) new JSONParser().parse(new InputStreamReader(response));
        System.out.println(object);
      } catch (Exception e) {
        try {
          InputStream response = connection.getErrorStream();
          JSONObject object = (JSONObject) new JSONParser().parse(new InputStreamReader(response));

          System.err.println();
          System.out.println("ERROR STREAM:");
          System.out.println(object);
        } catch (IOException e2) {
          System.err.println("IOException trying to print the error stream :(");
        } catch (org.json.simple.parser.ParseException e2) {
          System.err.println("ParseException trying to print the error stream :(");
        }

        System.err.println();
        System.err.println("EXCEPTION MESSAGE:");
        System.err.println(e.getMessage());

        System.err.println();
        System.err.println("EXCEPTION STACK TRACE:");
        e.printStackTrace(System.err);

        System.exit(1);
      }
    }
  }

} // Net


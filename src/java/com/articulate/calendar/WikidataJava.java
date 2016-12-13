package com.articulate.calendar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class WikidataJava {
  public WikidataJava(String dumpDir) throws IOException
  {
    if (dumpDir != null)
      loadFromDump(dumpDir);
  }

  public static class Item {
    public Item(int id, String enLabel)
    {
     	Id = id;
     	label_ = enLabel;
    }

    public void
    addHasInstance(int id)
    {
      if (hasInstance_ == null)
        hasInstance_ = new HashSet<Integer>();
      hasInstance_.add(id);
    }
    public void
    addHasSubclass(int id)
    {
      if (hasSubclass_ == null)
        hasSubclass_ = new HashSet<Integer>();
      hasSubclass_.add(id);
    }
    public void
    addHasPart(int id)
    {
      if (hasPart_ == null)
        hasPart_ = new HashSet<Integer>();
      hasPart_.add(id);
    }

    public String
    getEnLabel()
    {
      if (!labelHasId_)
        return label_;
      else {
        // Need to strip the id.
        if (label_.startsWith("Q") && !label_.contains(" "))
          return "";
        else
          return label_.substring(0, label_.lastIndexOf(" ("));
      }
    }

    public String
    getEnLabelWithId()
    {
      if (!labelHasId_) {
        if (label_.length() == 0)
          // Don't use up memory with just the Q ID string.
          return "Q" + Id;
        else {
          // Add the Id to label_.
          labelHasId_ = true;
          label_ = label_ + " (Q" + Id + ")";
        }
      }

      return label_;
    }

    @Override
    public String
    toString() { return getEnLabelWithId(); }

    public final int Id;
    public int[] instanceOf_ = null;
    public Set<Integer> hasInstance_ = null;
    public int[] subclassOf_ = null;
    public Set<Integer> hasSubclass_ = null;
    public int[] partOf_ = null;
    public Set<Integer> hasPart_ = null;
    public int[] saidToBeTheSameAs_ = null;
    public int[] country_ = null;
    public Map<Integer, Map<Integer, int[]>> countryQualifiers_ = null;
    public int[] locatedInTheAdministrativeTerritorialEntity_ = null;
    public Map<Integer, Map<Integer, int[]>> locatedInTheAdministrativeTerritorialEntityQualifiers_ = null;
    public int[] locatedInTimeZone_ = null;
    public Map<Integer, Map<Integer, int[]>> locatedInTimeZoneQualifiers_ = null;
    public Set<Integer> debugRootClasses_ = null;
    public boolean hasSubclassOfLoop_ = false;
    public boolean hasPartOfLoop_ = false;
    public boolean hasCountryLoop_ = false;
    public boolean hasLocatedInTheAdministrativeTerritorialEntityLoop_ = false;
    private String label_;
    private boolean labelHasId_ = false;

    public static int[] getInstanceOf(Item item) { return item.instanceOf_; }
    public static void setInstanceOf(Item item, int[] values) { item.instanceOf_ = values; }
    public static Set<Integer> getHasInstance(Item item) { return item.hasInstance_; }
    public static int[] getSubclassOf(Item item) { return item.subclassOf_; }
    public static void setSubclassOf(Item item, int[] values) { item.subclassOf_ = values; }
    public static Set<Integer> getHasSubclass(Item item) { return item.hasSubclass_; }
    public static int[] getPartOf(Item item) { return item.partOf_; }
    public static void setPartOf(Item item, int[] values) { item.partOf_ = values; }
    public static Set<Integer> getHasPart(Item item) { return item.hasPart_; }
    public static int[] getCountry(Item item) { return item.country_; }
    public static void setCountry(Item item, int[] values) { item.country_ = values; }
    public static int[] getLocatedInTheAdministrativeTerritorialEntity(Item item) { return item.locatedInTheAdministrativeTerritorialEntity_; }
    public static void setLocatedInTheAdministrativeTerritorialEntity(Item item, int[] values) { item.locatedInTheAdministrativeTerritorialEntity_ = values; }
    public static int[] getLocatedInTimeZone(Item item) { return item.locatedInTimeZone_; }
    public static void setLocatedInTimeZone(Item item, int[] values) { item.locatedInTimeZone_ = values; }

    public interface SetHasLoop { void setHasLoop(Item item, boolean hasLoop); }
    public static void setHasSubclassOfLoop(Item item, boolean hasLoop) { item.hasSubclassOfLoop_ = hasLoop; }
    public static void setHasPartOfLoop(Item item, boolean hasLoop) { item.hasPartOfLoop_ = hasLoop; }
    public static void setHasCountryLoop(Item item, boolean hasLoop) { item.hasCountryLoop_ = hasLoop; }
    public static void setHasLocatedInTheAdministrativeTerritorialEntityLoop(Item item, boolean hasLoop) { item.hasLocatedInTheAdministrativeTerritorialEntityLoop_ = hasLoop; }

    public interface GetHasLoop { boolean getHasLoop(Item item); }
    public static boolean getHasSubclassOfLoop(Item item) { return item.hasSubclassOfLoop_; }
    public static boolean getHasPartOfLoop(Item item) { return item.hasPartOfLoop_; }
    public static boolean getHasCountryLoop(Item item) { return item.hasCountryLoop_; }
    public static boolean getHasLocatedInTheAdministrativeTerritorialEntityLoop(Item item) { return item.hasLocatedInTheAdministrativeTerritorialEntityLoop_; }
  }

  public static class Property
  {
    public Property(int id, String enLabel)
    {
      Id = id;
      label_ = enLabel;
    }

    public String
    getEnLabel() { return label_; }

    public String
    getEnLabelOrId()
    {
      if (label_.length() == 0)
        return "P" + Id;
      else
        return label_;
    }

    @Override
    public String
    toString() { return getEnLabelOrId(); }

    public int[] subpropertyOf_ = null;
    public Datatype datatype_ = Datatype.WikibaseItem;
    public final int Id;
    private final String label_;

    public static int[] getSubpropertyOf(Property property) { return property.subpropertyOf_; }
    public static void setSubpropertyOf(Property property, int[] values) { property.subpropertyOf_ = values; }
  }

  public enum Datatype
  {
    WikibaseItem, WikibaseProperty, GlobeCoordinate, Quantity, Time, Url,
    String, MonolingualText, CommonsMedia, ExternalIdentifier,
    MathematicalExpression
  }

  public static final HashMap<Datatype, String> DatatypeString = new HashMap<>();;
  static {
    DatatypeString.put(Datatype.WikibaseItem, "wikibase-item");
    DatatypeString.put(Datatype.WikibaseProperty, "wikibase-property");
    DatatypeString.put(Datatype.GlobeCoordinate, "globe-coordinate");
    DatatypeString.put(Datatype.Quantity, "quantity");
    DatatypeString.put(Datatype.Time, "time");
    DatatypeString.put(Datatype.Url, "url");
    DatatypeString.put(Datatype.String, "string");
    DatatypeString.put(Datatype.MonolingualText, "monolingualtext");
    DatatypeString.put(Datatype.CommonsMedia, "commonsMedia");
    DatatypeString.put(Datatype.ExternalIdentifier, "external-id");
    DatatypeString.put(Datatype.MathematicalExpression, "math");
  }

  public static Datatype
  getDatatypeFromString(String datatypeString)
  {
    for (Map.Entry<Datatype, String> entry : DatatypeString.entrySet()) {
      if (entry.getValue().equals(datatypeString))
        return entry.getKey();
    }

    throw new Error("Unrecognized Datatype string: " + datatypeString);
  }

  public static void
  dumpFromJson(String gzipFilePath, String dumpDir, ArrayList<String> messages)
    throws FileNotFoundException, IOException
  {
    HashMap<Integer, Item> items = new HashMap<>();
    HashMap<Integer, Property> properties = new HashMap<>();
    int nLines = 0;
    long startMs = System.currentTimeMillis();

    try (FileInputStream fileIn = new FileInputStream(gzipFilePath);
         GZIPInputStream zipIn = new GZIPInputStream(fileIn);
         InputStreamReader isReader = new InputStreamReader(zipIn);
         BufferedReader reader = new BufferedReader(isReader)) {
      String line;
      while ((line = reader.readLine()) != null) {
        ++nLines;
        if (nLines % 500000 == 0) {
          System.out.println("nLines " + nLines + ", total memory GB " +
            Runtime.getRuntime().totalMemory() / 1000000000.0);
        }

        processLine(line, nLines, items, properties, messages);
      }
    }

    System.out.println("nLines " + nLines + ", elapsed minutes " +
      (System.currentTimeMillis() - startMs) / 60000.0);

    System.out.print("Writing dump files ...");
    try (FileWriter file = new FileWriter(new File(dumpDir, "itemEnLabels.tsv"));
         BufferedWriter writer = new BufferedWriter(file)) {
      for (Map.Entry<Integer, Item> entry : items.entrySet()) {
        // Json-encode the value, omitting surrounding quotes.
        String jsonString = gson_.toJson(entry.getValue().getEnLabel());
        writer.write(entry.getKey() + "\t" + jsonString.substring(1, jsonString.length() - 1));
        writer.newLine();
      }
    }

    dumpProperty
      (items, (Item obj) -> obj.instanceOf_,
       new File(dumpDir, "instanceOf.tsv").getAbsolutePath());
    dumpProperty
      (items, (Item obj) -> obj.subclassOf_,
       new File(dumpDir, "subclassOf.tsv").getAbsolutePath());
    dumpProperty
      (items, (Item obj) -> obj.partOf_,
       new File(dumpDir, "partOf.tsv").getAbsolutePath());
    dumpProperty
      (items, (Item obj) -> obj.saidToBeTheSameAs_,
       new File(dumpDir, "saidToBeTheSameAs.tsv").getAbsolutePath());
    dumpProperty
      (items, (Item obj) -> obj.country_,
       new File(dumpDir, "country.tsv").getAbsolutePath());
    dumpQualifiers
      (items, (Item obj) -> obj.countryQualifiers_,
       new File(dumpDir, "countryQualifiers.tsv").getAbsolutePath());
    dumpProperty
      (items, (Item obj) -> obj.locatedInTheAdministrativeTerritorialEntity_,
       new File(dumpDir, "locatedInTheAdministrativeTerritorialEntity.tsv").getAbsolutePath());
    dumpQualifiers
      (items, (Item obj) -> obj.locatedInTheAdministrativeTerritorialEntityQualifiers_,
       new File(dumpDir, "locatedInTheAdministrativeTerritorialEntityQualifiers.tsv").getAbsolutePath());
    dumpProperty
      (items, (Item obj) -> obj.locatedInTimeZone_,
       new File(dumpDir, "locatedInTimeZone.tsv").getAbsolutePath());
    dumpQualifiers
      (items, (Item obj) -> obj.locatedInTimeZoneQualifiers_,
       new File(dumpDir, "locatedInTimeZoneQualifiers.tsv").getAbsolutePath());

    try (FileWriter file = new FileWriter(new File(dumpDir, "propertyEnLabels.tsv"));
         BufferedWriter writer = new BufferedWriter(file)) {
      for (Map.Entry<Integer, Property> entry : properties.entrySet()) {
        // Json-encode the value, omitting surrounding quotes.
        String jsonString = gson_.toJson(entry.getValue().getEnLabel());
        writer.write(entry.getKey() + "\t" + jsonString.substring(1, jsonString.length() - 1));
        writer.newLine();
      }
    }

    try (FileWriter file = new FileWriter(new File(dumpDir, "propertyDatatype.tsv"));
         BufferedWriter writer = new BufferedWriter(file)) {
      for (Map.Entry<Integer, Property> entry : properties.entrySet()) {
        writer.write(entry.getKey() + "\t" + DatatypeString.get(entry.getValue().datatype_));
        writer.newLine();
      }
    }

    dumpProperty
      (properties, (Property obj) -> obj.subpropertyOf_,
       new File(dumpDir, "propertySubpropertyOf.tsv").getAbsolutePath());

    System.out.println(" done.");
  }

  public static void
  getStatistics(Map<Integer, Item> items, List<String> messages)
  {
    int nMultiSubclassOf = 0;
    int nSubclassOfAndInstanceOf = 0;
    int nNoInstanceOf = 0;
    int nEnClassWithNonEntityRoot = 0;
    int nHasSubclassOfLoop = 0;
    int nHasPartOfLoop = 0;
    int nHasCountryLoop = 0;
    int nHasLocatedInTheAdministrativeTerritorialEntityLoop = 0;
    int nItemsWithoutEnLabel = 0;
    int nClasses = 0;
    int nClassesWithoutEnLabel = 0;
    int nPartOf = 0;

    List<Integer> itemChain = new ArrayList<>();
    Map<Integer, int[]> subclassOfLoopItems = new HashMap<>();
    Map<Integer, int[]> partOfLoopItems = new HashMap<>();
    Map<Integer, int[]> countryLoopItems = new HashMap<>();
    Map<Integer, int[]> locatedInTheAdministrativeTerritorialEntityLoopItems = new HashMap<>();

    Set<Integer> entitySet = new HashSet<>();
    entitySet.add(QEntity);

    // Add entity since it does not have a subclass of property.
    ++nClasses;

    int itemCount = 0;
    for (Map.Entry<Integer, Item> entry : items.entrySet()) {
      Item item = entry.getValue();

      ++itemCount;
      if (itemCount % 5000000 == 0)
        System.out.println("Processing item " + itemCount + " of " + items.size());

/*
      if (item.hasInstance_ != null && item.hasInstance_.size() > 250000)
        Console.Out.WriteLine("\rhasInstance count " + item.hasInstance_.Count + ": " + item);
*/

      if (item.getEnLabel().isEmpty())
        ++nItemsWithoutEnLabel;

      if (item.subclassOf_ != null) {
        ++nClasses;

        if (item.getEnLabel().isEmpty())
          ++nClassesWithoutEnLabel;
        if (item.subclassOf_.length > 1)
          ++nMultiSubclassOf;
        if (item.instanceOf_ != null)
          ++nSubclassOfAndInstanceOf;
      }

      if (item.instanceOf_ != null) {
        for (int value : item.instanceOf_) {
          if (!items.containsKey(value))
            messages.add(item + " instance of non-existing Q" + value);
        }
      }
      else
        ++nNoInstanceOf;

      // TODO: Check for subproperty of non-existing property.

      if (item.partOf_ != null)
        ++nPartOf;

      // Get subclass of loops.
      if (item.subclassOf_ != null) {
        if (item.debugRootClasses_ == null)
          item.debugRootClasses_ = new HashSet<>();
        itemChain.clear();

        // TODO: This computes hasSubclassOfLoop_ which should be required.
        addRootItems
          (item, item.debugRootClasses_, items, entry.getKey(), itemChain,
           subclassOfLoopItems, (Item obj) -> obj.subclassOf_,
           "subclass of", (Item obj) -> obj.hasSubclassOfLoop_,
           (Item obj, boolean x) -> { obj.hasSubclassOfLoop_ = x; },
           messages);

        if (item.hasSubclassOfLoop_)
          ++nHasSubclassOfLoop;
        else {
          if (!item.getEnLabel().isEmpty() && 
              !item.debugRootClasses_.equals(entitySet))
            ++nEnClassWithNonEntityRoot;
        }
      }

      // Get part of loops.
      if (item.partOf_ != null) {
        itemChain.clear();

        // TODO: This computes hasPartOfLoop_ which should be required.
        addRootItems
          (item, null, items, entry.getKey(), itemChain, partOfLoopItems,
           (Item obj) -> obj.partOf_, "part of",
           (Item obj) -> obj.hasPartOfLoop_,
           (Item obj, boolean x) -> { obj.hasPartOfLoop_ = x; },
           messages);

        if (item.hasPartOfLoop_)
          ++nHasPartOfLoop;
      }

      // Get country loops.
      if (item.country_ != null) {
        itemChain.clear();

        // TODO: This computes hasCountryLoop_ which should be required.
        addRootItems
          (item, null, items, entry.getKey(), itemChain, countryLoopItems,
           (Item obj) -> obj.country_, "country",
           (Item obj) -> obj.hasCountryLoop_,
           (Item obj, boolean x) -> { obj.hasCountryLoop_ = x; },
           messages);

        if (item.hasCountryLoop_)
          ++nHasCountryLoop;
      }

      // Get located in the administrative territorial entity loops.
      if (item.locatedInTheAdministrativeTerritorialEntity_ != null) {
        itemChain.clear();

        // TODO: This computes hasLocatedInTheAdministrativeTerritorialEntityLoop_ which should be required.
        addRootItems
          (item, null, items, entry.getKey(), itemChain,
           locatedInTheAdministrativeTerritorialEntityLoopItems,
           (Item obj) -> debugGetLocatedInTheAdministrativeTerritorialEntityAndSubProperties(obj),
           "located in the administrative territorial entity",
           (Item obj) -> obj.hasLocatedInTheAdministrativeTerritorialEntityLoop_,
           (Item obj, boolean x) -> { obj.hasLocatedInTheAdministrativeTerritorialEntityLoop_ = x; },
           messages);

        if (item.hasLocatedInTheAdministrativeTerritorialEntityLoop_)
          ++nHasLocatedInTheAdministrativeTerritorialEntityLoop;
      }
    }

    System.out.print("Checking time zones ...");
    for (Map.Entry<Integer, Item> entry : items.entrySet()) {
      Item item = entry.getValue();
      if (item.locatedInTimeZone_ != null) {
        for (int timeZoneId : item.locatedInTimeZone_) {
          if (item.locatedInTimeZoneQualifiers_ != null &&
              item.locatedInTimeZoneQualifiers_.containsKey(timeZoneId) &&
              item.locatedInTimeZoneQualifiers_.get(timeZoneId).containsKey(PappliesToPart)) {
            for (int appliesToPartValue : item.locatedInTimeZoneQualifiers_.get(timeZoneId).get(PappliesToPart)) {
              if (items.get(appliesToPartValue).locatedInTimeZone_ == null)
                messages.add("Item " + item + " time zone applies to part without time zone: " + items.get(appliesToPartValue));
            }
          }

          Item timeZone = items.get(timeZoneId);
          if (!(timeZone.instanceOf_ != null &&
                (contains(timeZone.instanceOf_, QTimeZone) ||
                 contains(timeZone.instanceOf_, QTimeZoneNamedForAUtcOffset) ||
                 contains(timeZone.instanceOf_, QSeasonalTimeZone))))
            messages.add("*** Item " + item + " time zone value is not an instance of a type of TimeZone: " + timeZone);
        }
      }
    }
    System.out.println(" done.");

    for (int[] chain : subclassOfLoopItems.values()) {
      String message = "subclassOf loop";
      for (int id : chain)
        message += ", " + items.get(id);
      messages.add(message);
    }
    for (int[] chain : partOfLoopItems.values()) {
      String message = "partOf loop";
      for (int id : chain)
        message += ", " + items.get(id);
      messages.add(message);
    }
    for (int[] chain : countryLoopItems.values()) {
      String message = "country loop";
      for (int id : chain)
        message += ", " + items.get(id);
      messages.add(message);
    }
    for (int[] chain : locatedInTheAdministrativeTerritorialEntityLoopItems.values()) {
      String message = "locatedInTheAdministrativeTerritorialEntity loop";
      for (int id : chain)
        message += ", " + items.get(id);
      messages.add(message);
    }

    messages.add("nItems " + items.size() + ", nClasses " + nClasses +
      ", nPartOf " + nPartOf + ", nItemsWithoutEnLabel " + nItemsWithoutEnLabel +
      ", nClassesWithoutEnLabel " + nClassesWithoutEnLabel);
    messages.add("nMultiSubclassOf " + nMultiSubclassOf +
      ", nSubclassOfAndInstanceOf " + nSubclassOfAndInstanceOf +
      ", nNoInstanceOf " + nNoInstanceOf + ", nEnClassWithNonEntityRoot " +
      nEnClassWithNonEntityRoot);
    messages.add("nHasSubclassOfLoop " + nHasSubclassOfLoop +
      ", nHasPartOfLoop " + nHasPartOfLoop + ", nHasCountryLoop " +
      nHasCountryLoop + ", nHasLocatedInTheAdministrativeTerritorialEntityLoop " +
      nHasLocatedInTheAdministrativeTerritorialEntityLoop);
  }

  /**
   * Get the valid time zone for each location
   * @param items The map of Item ID with its Item.
   * @return A map where the key is the Item ID of a location and the value
   * is its time zone ID.
   */
  public static Map<Integer, Integer>
  getLocationTimeZones(Map<Integer, Item> items)
  {
    Map<Integer, Integer> result = new HashMap<>();

    for (Map.Entry<Integer, Item> entry : items.entrySet()) {
      Item item = entry.getValue();
      int timeZoneId = getUtcTimeZoneWithParentLocation(item, items);
      if (timeZoneId < 0)
        continue;

      result.put(entry.getKey(), timeZoneId);
    }

    return result;
  }

  /**
   * Get the time zone ID of the item or one of its parent locations where the
   * time zone is the unique value which is not for daylight saving. Time
   * promotes a time zone like "Central European Time" to the time zone with
   * UTC offset. This finds a parent location using
   * locatedInTheAdministrativeTerritorialEntity_ or country_.
   * @param item The Item to check.
   * @param items The Items map for looking up the time zone with UTC offset.
   * @return The time zone's Item ID, or -1 if not found.
   */
  private static int
  getUtcTimeZoneWithParentLocation(Item item, Map<Integer, Item> items)
  {
    Item currentItem = item;
    while (true) {
      int timeZoneId = getUtcTimeZone(currentItem, items);
      if (timeZoneId >= 0)
        return timeZoneId;

      if (currentItem.hasLocatedInTheAdministrativeTerritorialEntityLoop_)
        return -1;

      // TODO: Get the one with OK qualifiers.
      int[] locatedIn = currentItem.locatedInTheAdministrativeTerritorialEntity_ != null ?
        currentItem.locatedInTheAdministrativeTerritorialEntity_ : currentItem.country_;
      if (!(locatedIn != null && locatedIn.length == 1))
        // TODO: Search among mulitple parents.
        return -1;

      currentItem = items.get(locatedIn[0]);
      if (currentItem == null)
        return -1;
    }
  }

  /**
   * Get the item's time zone ID where the time zone is the unique value
   * which is not for daylight saving. Time promotes a time zone like
   * "Central European Time" to the time zone with UTC offset. This does not
   * check "parent" items that this may be located in.
   * @param item The Item to check.
   * @param items The Items map for looking up the time zone with UTC offset.
   * @return The time zone's Item ID, or -1 if not found.
   */
  private static int
  getUtcTimeZone(Item item, Map<Integer, Item> items)
  {
    if (item.locatedInTimeZone_ == null)
      return -1;

    int result = -1;
    for (int timeZone : item.locatedInTimeZone_) {
      // Try to disqulaify based on qualifiers.
      if (item.locatedInTimeZoneQualifiers_ != null &&
          item.locatedInTimeZoneQualifiers_.containsKey(timeZone)) {
        boolean isValid = true;
        for (Map.Entry<Integer, int[]> entry 
             : item.locatedInTimeZoneQualifiers_.get(timeZone).entrySet()) {
          if (entry.getKey() == PvalidInPeriod) {
            if (entry.getValue().length != 1) {
              System.out.println
                ("Item " + item + " has multiple time zone valid in period values");
              isValid = false;
              break;
            }

            if (entry.getValue()[0] == QStandardTime) {
              // Standard time is OK.
            }
            else if (entry.getValue()[0] == QDaylightSavingTime) {
              // Reject time zones for daylight saving time.
              isValid = false;
              break;
            }
            else {
              System.out.println
                ("Item " + item + " has an unrecognized time zone valid in period value " + entry.getValue()[0]);
              // TODO: We should explicitly recognize unknown values to reject.
              isValid = false;
              break;
            }
          }
          else if (entry.getKey() == PstartTime) {
            // A start time is OK. We reject an end time below.
          }
          else if (entry.getKey() == PendTime) {
            // Reject an entry with an end time qualifier (assuming the end time is in the past).
            isValid = false;
            break;
          }
          else if (entry.getKey() == PappliesToPart) {
            // Reject a time zone with applies to part. Assume the part has its own.
            isValid = false;
            break;
          }
          else if (entry.getKey() == PsubjectOf) {
            // A subject of qualifier is OK, assuming it is a comment.
          }
          else
            throw new Error
              ("Item " + item + " has an unrecognized time zone qualifier " + entry.getKey());
        }

        if (!isValid)
          // Try the next time zone.
          continue;
      }

      // The timeZone is valid.
      int timeZoneWithUtcOffset = getTimeZoneWithUtcOffset(timeZone, items);
      if (timeZoneWithUtcOffset < 0) {
        System.out.println("Item " + item + " has no time zone with UTC offset");
        continue;
      }

      if (result >= 0 && result != timeZoneWithUtcOffset) {
        System.out.println("Item " + item + " has multiple valid time zones " +
          result + " and " + timeZoneWithUtcOffset);
        // Ignore multiple valid results.
        return -1;
      }
      result = timeZoneWithUtcOffset;
    }

    return result;
  }

  /**
   * Get the item's time zone ID where the time zone is the unique value
   * which is not for daylight saving. Time promotes a time zone like
   * "Central European Time" to the time zone with UTC offset. This does not
   * check "parent" items that this may be located in.
   * @param timeZoneId The ID of the time zone Item to check.
   * @param items The Items map for looking up the time zone with UTC offset.
   * @return The time zone's Item ID, or -1 if not found.
   */
  private static int
  getTimeZoneWithUtcOffset(int timeZoneId, Map<Integer, Item> items)
  {
    Item timeZone = items.get(timeZoneId);
    if (timeZone.instanceOf_ != null &&
        contains(timeZone.instanceOf_, QTimeZoneNamedForAUtcOffset))
      return timeZoneId;

    // For now, only check one level of indirection.
    int parentTimeZoneId;
    if (timeZone.locatedInTimeZone_ != null &&
        timeZone.locatedInTimeZone_.length == 1 &&
        timeZone.locatedInTimeZoneQualifiers_ == null)
      parentTimeZoneId = timeZone.locatedInTimeZone_[0];
    else if (timeZone.saidToBeTheSameAs_ != null &&
             timeZone.saidToBeTheSameAs_.length == 1)
      parentTimeZoneId = timeZone.saidToBeTheSameAs_[0];
    else
      parentTimeZoneId = -1;

    if (parentTimeZoneId >= 0) {
      Item parentTimeZone = items.get(parentTimeZoneId);
      if (parentTimeZone.instanceOf_ != null &&
          contains(parentTimeZone.instanceOf_, QTimeZoneNamedForAUtcOffset))
        return parentTimeZoneId;
      else {
        System.out.println("The parent time zone of " + timeZone +
          " is not a time zone with UTC offset");
        return -1;
      }
    }
    else {
      System.out.println("Time zone " + timeZone + " does not have a parent time zone");
      return -1;
    }
  }

  private static int[]
  debugGetLocatedInTheAdministrativeTerritorialEntityAndSubProperties(Item item)
  {
    if (item.locatedInTheAdministrativeTerritorialEntity_ == null &&
        item.country_ == null)
      return null;
    else if (item.locatedInTheAdministrativeTerritorialEntity_ == null)
      return item.country_;
    else if (item.country_ == null)
      return item.locatedInTheAdministrativeTerritorialEntity_;
    else {
      int[] result =
        new int[item.locatedInTheAdministrativeTerritorialEntity_.length +
                item.country_.length];
      int iTo = 0;
      for (int x : item.locatedInTheAdministrativeTerritorialEntity_)
        result[iTo++] = x;
      for (int x : item.country_)
        result[iTo++] = x;

      return result;
    }
  }

  private static void
  addRootItems
    (Item leafItem, Set<Integer> leafItemRootItems, Map<Integer, Item> items,
     int itemId, List<Integer> itemChain, Map<Integer, int[]> loopItems,
     GetIntArray<Item> getPropertyValues, String propertyLabel,
     Item.GetHasLoop getHasLoop, Item.SetHasLoop setHasLoop,
     List<String> messages)
  {
    Item item = items.get(itemId);

    if (itemChain.contains(itemId)) {
      itemChain.add(itemId);
      loopItems.put(itemId, listToArray(itemChain));
      setHasLoop.setHasLoop(leafItem, true);
      return;
    }
    itemChain.add(itemId);

    int[] propertyValues = getPropertyValues.getIntArray(item);
    if (propertyValues == null) {
      // No more property values in the chain. Finished.
      if (leafItemRootItems != null)
        leafItemRootItems.add(itemId);
      return;
    }

    int saveItemChainSize = itemChain.size();
    for (int propertyValue : propertyValues) {
      if (propertyValue == itemId) {
        // A property value is itself. Finished.
        if (leafItemRootItems != null)
          leafItemRootItems.add(itemId);
      }
      else {
        // Resize to saveItemChainSize to start over.
        while (itemChain.size() > saveItemChainSize)
          itemChain.remove(saveItemChainSize);

        // Recurse.
        if (!items.containsKey(propertyValue))
          messages.add
            (items.get(itemId) + " " + propertyLabel + " non-existing Q" +
             propertyValue);
        else
          addRootItems
            (leafItem, leafItemRootItems, items, propertyValue, itemChain,
             loopItems, getPropertyValues, propertyLabel, getHasLoop,
             setHasLoop, messages);

        if (getHasLoop.getHasLoop(leafItem))
          // There is a loop. We don't need to continue.
          return;
      }
    }
  }

  private static <T> void
  dumpProperty
    (Map<Integer, T> dictionary, GetIntArray<T> getPropertyValues, String filePath)
    throws IOException
  {
    try (FileWriter file = new FileWriter(filePath);
         BufferedWriter writer = new BufferedWriter(file)) {
      for (Map.Entry<Integer, T> entry : dictionary.entrySet()) {
        if (getPropertyValues.getIntArray(entry.getValue()) != null) {
          writer.write("" + entry.getKey());
          for (int value : getPropertyValues.getIntArray(entry.getValue()))
            writer.write("\t" + value);
          writer.newLine();
        }
      }
    }
  }

  private static <T> void
  dumpQualifiers
    (Map<Integer, T> dictionary, GetQualifiersMap<T> getQualifiers,
     String filePath) throws IOException
  {
    try (FileWriter file = new FileWriter(filePath);
         BufferedWriter writer = new BufferedWriter(file)) {
      // itemId\titemPropertyId\tqualifierPropertyId1\titemValue1\titemValue2...
      for (Map.Entry<Integer, T> entry : dictionary.entrySet()) {
        if (getQualifiers.getQualifiersMap(entry.getValue()) != null) {
          for (Map.Entry<Integer, Map<Integer, int[]>> qualifiersEntry
               : getQualifiers.getQualifiersMap(entry.getValue()).entrySet()) {
            for (Map.Entry<Integer, int[]> qualifierValuesEntry
                 : qualifiersEntry.getValue().entrySet()) {
              writer.write
                (entry.getKey() + "\t" + qualifiersEntry.getKey() + "\t" +
                 qualifierValuesEntry.getKey());
              for (int value : qualifierValuesEntry.getValue())
                writer.write("\t" + value);
              writer.newLine();
            }
          }
        }
      }
    }
  }

  private void
  loadFromDump(String dumpDir) throws FileNotFoundException, IOException
  {
    try (FileReader file = new FileReader(new File(dumpDir, "itemEnLabels.tsv"));
         BufferedReader reader = new BufferedReader(file)) {
      int nLines = 0;
      String line;
      while ((line = reader.readLine()) != null) {
        ++nLines;
        if (nLines % 5000000 == 0) {
          System.out.println("N itemEnLabels lines " + nLines + ", total memory GB " +
            Runtime.getRuntime().totalMemory() / 1000000000.0);
        }

        String[] splitLine = line.split("\\t");
        int id = Integer.parseInt(splitLine[0]);
        String label = splitLine.length < 2 ? ""
          : gson_.fromJson("\"" + splitLine[1] + "\"", String.class);
        if (!items_.containsKey(id))
          // Decode the Json value.
          items_.put(id, new Item(id, label));
      }
    }

    System.out.print("Loading propertyEnLabels ...");
    try (FileReader file = new FileReader(new File(dumpDir, "propertyEnLabels.tsv"));
         BufferedReader reader = new BufferedReader(file)) {
      int nLines = 0;
      String line;
      while ((line = reader.readLine()) != null) {
        ++nLines;
        if (nLines % 5000000 == 0) {
          System.out.println("N propertyEnLabels lines " + nLines + ", total memory GB " +
            Runtime.getRuntime().totalMemory() / 1000000000.0);
        }

        String[] splitLine = line.split("\\t");
        int id = Integer.parseInt(splitLine[0]);
        String label = splitLine.length < 2 ? ""
          : gson_.fromJson("\"" + splitLine[1] + "\"", String.class);
        if (!properties_.containsKey(id))
          // Decode the Json value.
          properties_.put(id, new Property(id, label));
      }
    }
    System.out.println(" done.");

    try (FileReader file = new FileReader(new File(dumpDir, "propertyDatatype.tsv"));
         BufferedReader reader = new BufferedReader(file)) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] splitLine = line.split("\\t");
        int id = Integer.parseInt(splitLine[0]);
        properties_.get(id).datatype_ = getDatatypeFromString(splitLine[1]);
      }
    }

    loadPropertyFromDump
      (new File(dumpDir, "instanceOf.tsv").getAbsolutePath(), items_, "instance of", 
       (Item obj, int[] x) -> { obj.instanceOf_ = x; });
    loadPropertyFromDump
      (new File(dumpDir, "subclassOf.tsv").getAbsolutePath(), items_, "subclass of", 
       (Item obj, int[] x) -> { obj.subclassOf_ = x; });
    loadPropertyFromDump
      (new File(dumpDir, "partOf.tsv").getAbsolutePath(), items_, "part of", 
       (Item obj, int[] x) -> { obj.partOf_ = x; });
    loadPropertyFromDump
      (new File(dumpDir, "saidToBeTheSameAs.tsv").getAbsolutePath(), items_, "said to be the same as",
       (Item obj, int[] x) -> { obj.saidToBeTheSameAs_ = x; });
    loadPropertyFromDump
      (new File(dumpDir, "country.tsv").getAbsolutePath(), items_, "country", 
       (Item obj, int[] x) -> { obj.country_ = x; });
    loadQualifiersFromDump
      (new File(dumpDir, "countryQualifiers.tsv").getAbsolutePath(), items_,
       "country", (Item obj) -> obj.countryQualifiers_,
       (Item obj, Map<Integer, Map<Integer, int[]>> x) -> { obj.countryQualifiers_ = x; });
    loadPropertyFromDump
      (new File(dumpDir, "locatedInTheAdministrativeTerritorialEntity.tsv").getAbsolutePath(), items_,
       "located in the administrative territorial entity", 
       (Item obj, int[] x) -> { obj.locatedInTheAdministrativeTerritorialEntity_ = x; });
    loadQualifiersFromDump
      (new File(dumpDir, "locatedInTheAdministrativeTerritorialEntityQualifiers.tsv").getAbsolutePath(), items_,
       "located in the administrative territorial entity",
       (Item obj) -> obj.locatedInTheAdministrativeTerritorialEntityQualifiers_,
       (Item obj, Map<Integer, Map<Integer, int[]>> x) -> { obj.locatedInTheAdministrativeTerritorialEntityQualifiers_ = x; });
    loadPropertyFromDump
      (new File(dumpDir, "locatedInTimeZone.tsv").getAbsolutePath(), items_,
       "located in time zone", 
       (Item obj, int[] x) -> { obj.locatedInTimeZone_ = x; });
    loadQualifiersFromDump
      (new File(dumpDir, "locatedInTimeZoneQualifiers.tsv").getAbsolutePath(), items_,
       "located in time zone",
       (Item obj) -> obj.locatedInTimeZoneQualifiers_,
       (Item obj, Map<Integer, Map<Integer, int[]>> x) -> { obj.locatedInTimeZoneQualifiers_ = x; });

    loadPropertyFromDump
      (new File(dumpDir, "propertySubpropertyOf.tsv").getAbsolutePath(), properties_,
       "subproperty of", 
       (Property obj, int[] x) -> { obj.subpropertyOf_ = x; });

    System.out.print("Finding instances, subclasses and parts ...");
    setHasInstanceHasSubclassAndHasPart();
    System.out.println(" done.");
  }

  private static <T> void
  loadPropertyFromDump
    (String filePath, Map<Integer, T> dictionary, String propertyLabel,
     SetIntArray<T> setPropertyValues) throws IOException
  {
    System.out.print("Loading property " + propertyLabel + " ...");

    try (FileReader file = new FileReader(filePath);
         BufferedReader reader = new BufferedReader(file)) {
      HashSet valueSet = new HashSet<>();
      String line;
      while ((line = reader.readLine()) != null) {
        String[] splitLine = line.split("\\t");
        T obj = dictionary.get(Integer.parseInt(splitLine[0]));

        valueSet.clear();
        for (int i = 1; i < splitLine.length; ++i)
          valueSet.add(Integer.parseInt(splitLine[i]));
        setPropertyValues.setIntArray(obj, setToArray(valueSet));
      }
    }

    System.out.println(" done.");
  }

  private static <T> void
  loadQualifiersFromDump
    (String filePath, Map<Integer, T> dictionary, String propertyLabel,
     GetQualifiersMap<T> getQualifiers, SetQualifiersMap<T> setQualifiers)
     throws IOException
  {
    System.out.print("Loading qualifiers for property " + propertyLabel + " ...");

    try (FileReader file = new FileReader(filePath);
         BufferedReader reader = new BufferedReader(file)) {
      HashSet valueSet = new HashSet<>();
      String line;
      while ((line = reader.readLine()) != null) {
        String[] splitLine = line.split("\\t");
        int itemId = Integer.parseInt(splitLine[0]);
        int itemPropertyId = Integer.parseInt(splitLine[1]);
        int qualifierPropertyId = Integer.parseInt(splitLine[2]);
        valueSet.clear();
        for (int i = 3; i < splitLine.length; ++i)
          valueSet.add(Integer.parseInt(splitLine[i]));

        T obj = dictionary.get(itemId);
        if (getQualifiers.getQualifiersMap(obj) == null)
          setQualifiers.setQualifiersMap(obj, new HashMap<>());

        Map<Integer, int[]> qualifierValuesMap =
          getQualifiers.getQualifiersMap(obj).get(itemPropertyId);
        if (qualifierValuesMap == null) {
          qualifierValuesMap = new HashMap<>();
          getQualifiers.getQualifiersMap(obj).put(itemPropertyId, qualifierValuesMap);
        }
        qualifierValuesMap.put(qualifierPropertyId, setToArray(valueSet));
      }
    }

    System.out.println(" done.");
  }

  private void
  setHasInstanceHasSubclassAndHasPart()
  {
    for (Item item : items_.values()) {
      if (item.instanceOf_ != null) {
        for (int id : item.instanceOf_) {
          Item value = items_.get(id);
          if (value != null)
            value.addHasInstance(item.Id);
        }
      }

      if (item.subclassOf_ != null) {
        for (int id : item.subclassOf_) {
          Item value = items_.get(id);
          if (value != null)
            value.addHasSubclass(item.Id);
        }
      }

      if (item.partOf_ != null) {
        for (int id : item.partOf_) {
          Item value = items_.get(id);
          if (value != null)
            value.addHasPart(item.Id);
        }
      }
    }
  }

  private static void
  processLine
    (String line, int nLines, Map<Integer, Item> items,
     Map<Integer, Property> properties, List<String> messages) throws IOException
  {
    // Assume one item or property per line.

    // Skip blank lines and the open/close of the outer list.
    if (line.length() == 0 || line.charAt(0) == '[' || line.charAt(0) == ']' ||
        line.charAt(0) == ',')
      return;

    Matcher matcher = itemPattern_.matcher(line);
    if (matcher.find()) {
      Item item = processItem(line, Integer.parseInt(matcher.group(1)), messages);
      if (items.containsKey(item.Id))
        messages.add(">>>>>> Replacing existing item " + items.get(item.Id));
      items.put(item.Id, item);
    }
    else {
      matcher = propertyPattern_.matcher(line);
      if (matcher.find()) {
        Property property = processProperty
          (line, Integer.parseInt(matcher.group(2)), matcher.group(1), messages);
        if (properties.containsKey(property.Id))
          messages.add("Already have property P" + property.Id + " \"" + 
            properties.get(property.Id) + "\". Got \"" + property + "\"");
        properties.put(property.Id, property);
      }
      else
        throw new Error
          ("Line " + nLines + " not an item or property: " +
           line.substring(0, Math.min(75, line.length())));
    }
  }

  private static Item
  processItem(String line, int id, List<String> messages) throws IOException
  {
    Item item = new Item(id, getEnLabel(line));
    List<Map<Integer, Map<Integer, int[]>>> qualifiers = new ArrayList<>();
    qualifiers.add(null);

    item.instanceOf_ = setToArray
      (getPropertyValues(item, "instance of", line, PinstanceOf, messages, false, null));
    item.subclassOf_ = setToArray
      (getPropertyValues(item, "subclass of", line, PsubclassOf, messages, false, null));
    item.partOf_ = setToArray
      (getPropertyValues(item, "part of", line, PpartOf, messages, false, null));
    item.saidToBeTheSameAs_ = setToArray
      (getPropertyValues(item, "said to be the same as", line, PsaidToBeTheSameAs, messages, false, null));
    item.country_ = setToArray
      (getPropertyValues(item, "country", line, Pcountry, messages, false, qualifiers));
    item.countryQualifiers_ = qualifiers.get(0);
    item.locatedInTheAdministrativeTerritorialEntity_ = setToArray
      (getPropertyValues(item, "located in the administrative territorial entity", line,
       PlocatedInTheAdministrativeTerritorialEntity, messages, false, qualifiers));
    item.locatedInTheAdministrativeTerritorialEntityQualifiers_ = qualifiers.get(0);
    item.locatedInTimeZone_ = setToArray(getPropertyValues
      (item, "located in time zone", line, PlocatedInTimeZone, messages, false,
       qualifiers));
    item.locatedInTimeZoneQualifiers_ = qualifiers.get(0);

    return item;
  }

  private static int[] setToArray(Set<Integer> set)
  {
    if (set == null)
      return null;

    int[] result = new int[set.size()];
    int i = 0;
    for (int x : set)
      result[i++] = x;
    return result;
  }

  private static int[] listToArray(List<Integer> list)
  {
    if (list == null)
      return null;

    int[] result = new int[list.size()];
    int i = 0;
    for (int x : list)
      result[i++] = x;
    return result;
  }

  private static Property
  processProperty
    (String line, int id, String datatypeString, List<String> messages) throws IOException
  {
    String enLabel = getEnLabel(line);
    if (enLabel == "")
      messages.add("No enLabel for property P" + id);
    Property property = new Property(id, enLabel);

    property.subpropertyOf_ = setToArray
      (getPropertyValues(null, "subproperty of", line, 1647, messages, true, null));
    property.datatype_ = getDatatypeFromString(datatypeString);

    return property;
  }

  private static Set<Integer>
  getPropertyValues
    (Item item, String propertyName, String line, int propertyId,
     List<String> messages, boolean objIsProperty,
     List<Map<Integer, Map<Integer, int[]>>> qualifiersReturn) throws IOException
  {
    String qualifiersStart = ",\"qualifiers\":";
    HashSet<Integer> valueSet = new HashSet<>();
    Map<Integer, Map<Integer, int[]>> qualifiers = new HashMap<>();

    Pattern pattern = Pattern.compile
      ("\"mainsnak\":\\{\"snaktype\":\"value\",\"property\":\"P" + propertyId +
       "\",\"datavalue\":\\{\"value\":\\{\"entity-type\":\"" + (objIsProperty ? "property" : "item") +
       "\",\"numeric-id\":(\\d+)" +
       // If not objIsProperty, scan up to the beginning of possible qualifiers.
       (objIsProperty ? "" : ",\"id\":\"Q\\d+\"},\"type\":\"wikibase-entityid\"},\"datatype\":\"wikibase-item\"},\"type\":\"statement\""));
    Matcher matcher = pattern.matcher(line);
    while (matcher.find()) {
      int value = Integer.parseInt(matcher.group(1));
      if (objIsProperty)
        // TODO: Check for property self reference.
        valueSet.add(value);
      else {
        if (value != item.Id) {
          int iQualifiersStartEnd = matcher.end(0) + qualifiersStart.length();
          boolean hasQualifiers = (iQualifiersStartEnd < line.length() &&
            line.regionMatches(matcher.end(0), qualifiersStart, 0, qualifiersStart.length()));
          if (hasQualifiers && qualifiersReturn != null) {
            Map<Integer, int[]> qualifiersValues = readQualifiers
              (line, iQualifiersStartEnd);
            if (qualifiersValues.size() > 0)
              qualifiers.put(value, qualifiersValues);
          }

          valueSet.add(value);
        }
        else {
          // Unfortunately, all countries are country themself, so don't show.
          if (!propertyName.equals("country"))
            messages.add("Item is " + propertyName + " itself: " + item);
        }
      }
    }

    if (qualifiersReturn != null)
      qualifiersReturn.set(0, qualifiers.size() > 0 ? qualifiers : null);

    if (valueSet.isEmpty())
      return null;
    else
      return valueSet;
  }

  /**
   * Read the JSON object which has multiple qualifiers.
   * @param json The string containing the JSON qualifiers.
   * @param iStart The starting index in json of the qualifiers.
   * @return A Map where the key is the qualifier property ID and the value is
   * an array of item ID values.
   */
  private static Map<Integer, int[]>
  readQualifiers(String json, int iStart) throws IOException
  {
    Map<Integer, int[]> result = new HashMap<>();

    try (StringReader stringReader = new StringReader(json)) {
      stringReader.skip(iStart);

      try (JsonReader reader = new JsonReader(stringReader)) {
        reader.beginObject();
        while (reader.hasNext()) {
          // Ignore the name like "P518". We'll get the propertyId below.
          reader.nextName();

          int previousPropertyId = -1;
          Set<Integer> valueItemIdSet = new HashSet<>();

          // Read the array of values.
          reader.beginArray();
          while (reader.hasNext()) {
            int propertyId = -1;
            int valueItemId = -1;

            // Read the value object.
            reader.beginObject();
            while (reader.hasNext()) {
              String name = reader.nextName();

              if (name.equals("property")) {
                String value = reader.nextString();
                if (value.startsWith("P")) {
                  propertyId = Integer.parseInt(value.substring(1));
                  
                  if (previousPropertyId < 0)
                    previousPropertyId = propertyId;
                  else {
                    if (propertyId != previousPropertyId)
                      // We don't expect this to happen.
                      throw new Error("Unexpected change in qualifier property ID");
                  }
                }
              }
              else if (name.equals("datavalue")) {
                // Read the datavalue object.
                reader.beginObject();
                while (reader.hasNext()) {
                  name = reader.nextName();

                  if (name.equals("value")) {
                    // Read the datavalue value object.
                    if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                      reader.beginObject();
                      while (reader.hasNext()) {
                        name = reader.nextName();

                        if (name.equals("id")) {
                          String value = reader.nextString();
                          if (value.startsWith("Q"))
                            valueItemId = Integer.parseInt(value.substring(1));
                        }
                        else
                          reader.skipValue();
                      }

                      reader.endObject();
                      if (valueItemId < 0)
                        valueItemId = QNull; // debug
                    }
                    else
                      reader.skipValue();
                  }
                  else
                    reader.skipValue();
                }

                reader.endObject();
              }
              else
                reader.skipValue();
            }

            reader.endObject();

            if (propertyId >= 0 && valueItemId >= 0)
              valueItemIdSet.add(valueItemId);
          }

          reader.endArray();

          if (previousPropertyId >= 0 && valueItemIdSet.size() > 0)
            result.put(previousPropertyId, setToArray(valueItemIdSet));
        }

        reader.endObject();
      }
    }

    return result;
  }

  private static String
  getEnLabel(String line)
  {
    int iLabelsStart = line.indexOf("\"labels\":{\"");
    if (iLabelsStart < 0)
      return "";

    // Debug: Problem if a label has "}}".
    int iLabelsEnd = line.indexOf("}}", iLabelsStart);
    if (iLabelsEnd < 0)
      return "";

    String enPrefix = "en\":{\"language\":\"en\",\"value\":\"";
    int iEnStart = line.indexOf(enPrefix, iLabelsStart);
    if (iEnStart >= iLabelsEnd)
      iEnStart = -1;
    if (iEnStart < 0)
      return "";

    int iEnLabelStart = iEnStart + enPrefix.length();
    // Find the end quote, skipping escaped characters.
    int iEndQuote = iEnLabelStart;
    while (true) {
      int iBackslash = line.indexOf('\\', iEndQuote);
      iEndQuote = line.indexOf('\"', iEndQuote);
      if (iBackslash >= 0 && iBackslash < iEndQuote)
        // Skip escaped characters.
        iEndQuote = iBackslash + 2;
      else
        break;
    }

    // Include the surrounding quotes.
    String jsonString = line.substring(iEnLabelStart - 1, iEndQuote + 1);
    // Decode the Json value.
    return gson_.fromJson(jsonString, String.class);
  }

  private static boolean contains(int[] array, int value) {
    for (int x : array) {
      if (x == value)
        return true;
    }
    return false;
  }

  public HashMap<Integer, Item> items_ = new HashMap<>();
  public HashMap<Integer, Property> properties_ = new HashMap<>();

  public interface GetIntArray<T> { int[] getIntArray(T obj); }
  public interface GetQualifiersMap<T> { Map<Integer, Map<Integer, int[]>> getQualifiersMap(T obj); }
  public interface SetIntArray<T> { void setIntArray(T obj, int[] values); }
  public interface SetQualifiersMap<T> { void setQualifiersMap(T obj, Map<Integer, Map<Integer, int[]>> values); }

  public static int QTimeZone = 12143;
  public static int QEntity = 35120;
  public static int QDaylightSavingTime = 36669;
  public static int QNull = 543287;
  public static int QStandardTime = 1777301;
  public static int QTimeZoneNamedForAUtcOffset = 17272482;
  public static int QIanaTimeZone = 17272692;
  public static int QSeasonalTimeZone = 17280916;
  public static int Pcountry = 17;
  public static int PinstanceOf = 31;
  public static int PlocatedInTheAdministrativeTerritorialEntity = 131;
  public static int PsubclassOf = 279;
  public static int PpartOf = 361;
  public static int PlocatedInTimeZone = 421;
  public static int PsaidToBeTheSameAs = 460;
  public static int PappliesToPart = 518;
  public static int PstartTime = 580;
  public static int PendTime = 582;
  public static int PsubjectOf = 805;
  public static int PvalidInPeriod = 1264;
  public static int PtimeZoneOffset = 2907;
  private static final Gson gson_ = new Gson();
  private static final Pattern itemPattern_ = Pattern.compile
    ("^\\{\"type\":\"item\",\"id\":\"Q(\\d+)");
  private static final Pattern propertyPattern_ = Pattern.compile
    ("^\\{\"type\":\"property\",\"datatype\":\"([\\w-]+)\",\"id\":\"P(\\d+)");
}

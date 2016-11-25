package com.articulate.calendar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
    public HashSet<Integer> hasInstance_ = null;
    public int[] subclassOf_ = null;
    public HashSet<Integer> hasSubclass_ = null;
    public int[] partOf_ = null;
    public HashSet<Integer> hasPart_ = null;
    public int[] country_ = null;
    public int[] locatedInTheAdministrativeTerritorialEntity_ = null;
    public int[] locatedInTimeZone_ = null;
    public HashSet<Integer> debugRootClasses_ = null;
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
      (items, (Item obj) -> obj.country_,
       new File(dumpDir, "country.tsv").getAbsolutePath());
    dumpProperty
      (items, (Item obj) -> obj.locatedInTheAdministrativeTerritorialEntity_,
       new File(dumpDir, "locatedInTheAdministrativeTerritorialEntity.tsv").getAbsolutePath());
    dumpProperty
      (items, (Item obj) -> obj.locatedInTimeZone_,
       new File(dumpDir, "locatedInTimeZone.tsv").getAbsolutePath());

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

  private static int[]
  debugGetLocatedInTheAdministrativeTerritorialEntityAndSubProperties(Item item)
  {
    if (item.locatedInTheAdministrativeTerritorialEntity_ == null)
      return null;
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

  private static <T> void dumpProperty
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
          System.out.println("N itemEnLabels lines " + nLines + ", totalMemory " +
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
      (new File(dumpDir, "country.tsv").getAbsolutePath(), items_, "country", 
       (Item obj, int[] x) -> { obj.country_ = x; });
    loadPropertyFromDump
      (new File(dumpDir, "locatedInTheAdministrativeTerritorialEntity.tsv").getAbsolutePath(), items_,
       "located in the administrative territorial entity", 
       (Item obj, int[] x) -> { obj.locatedInTheAdministrativeTerritorialEntity_ = x; });
    loadPropertyFromDump
      (new File(dumpDir, "locatedInTimeZone.tsv").getAbsolutePath(), items_,
       "located in time zone", 
       (Item obj, int[] x) -> { obj.locatedInTimeZone_ = x; });

    loadPropertyFromDump
      (new File(dumpDir, "propertySubpropertyOf.tsv").getAbsolutePath(), properties_,
       "subproperty of", 
       (Property obj, int[] x) -> { obj.subpropertyOf_ = x; });

    System.out.print("Finding instances, subclasses and parts ...");
    setHasInstanceHasSubclassAndHasPart();
    System.out.println(" done.");
  }

  private static <T> void loadPropertyFromDump
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

  private void setHasInstanceHasSubclassAndHasPart()
  {
    for (Item item : items_.values()) {
      if (item.instanceOf_ != null) {
        for (int id : item.instanceOf_) {
          Item value = items_.getOrDefault(id, null);
          if (value != null)
            value.addHasInstance(item.Id);
        }
      }

      if (item.subclassOf_ != null) {
        for (int id : item.subclassOf_) {
          Item value = items_.getOrDefault(id, null);
          if (value != null)
            value.addHasSubclass(item.Id);
        }
      }

      if (item.partOf_ != null) {
        for (int id : item.partOf_) {
          Item value = items_.getOrDefault(id, null);
          if (value != null)
            value.addHasPart(item.Id);
        }
      }
    }
  }

  private static void
  processLine
    (String line, int nLines, Map<Integer, Item> items,
     Map<Integer, Property> properties, List<String> messages)
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
  processItem(String line, int id, List<String> messages)
  {
    Item item = new Item(id, getEnLabel(line));

    item.instanceOf_ = setToArray
      (getPropertyValues(item, "instance of", line, PinstanceOf, messages, false));
    item.subclassOf_ = setToArray
      (getPropertyValues(item, "subclass of", line, PsubclassOf, messages, false));
    item.partOf_ = setToArray
      (getPropertyValues(item, "part of", line, PpartOf, messages, false));
    item.country_ = setToArray
      (getPropertyValues(item, "country", line, Pcountry, messages, false));
    item.locatedInTheAdministrativeTerritorialEntity_ = setToArray
      (getPropertyValues(item, "located in the administrative territorial entity", line,
       PlocatedInTheAdministrativeTerritorialEntity, messages, false));
    item.locatedInTimeZone_ = setToArray(getPropertyValues
      (item, "located in time zone", line, PlocatedInTimeZone, messages, false));

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
    (String line, int id, String datatypeString, List<String> messages)
  {
    String enLabel = getEnLabel(line);
    if (enLabel == "")
      messages.add("No enLabel for property P" + id);
    Property property = new Property(id, enLabel);

    property.subpropertyOf_ = setToArray
      (getPropertyValues(null, "subproperty of", line, 1647, messages, true));
    property.datatype_ = getDatatypeFromString(datatypeString);

    return property;
  }

  private static Set<Integer>
  getPropertyValues
    (Item item, String propertyName, String line, int propertyId,
     List<String> messages, boolean objIsProperty)
  {
    String qualifiersStart = ",\"qualifiers\":";
    HashSet<Integer> valueSet = new HashSet<>();

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
        int iMatchEnd = matcher.end(0) + matcher.group(0).length();
        if (value != item.Id) {
          // Debug: Taking the substring is inefficient.
          boolean hasQualifiers = (iMatchEnd < line.length() &&
            line.substring(iMatchEnd).startsWith(qualifiersStart));
          if (hasQualifiers) {
/*
            if (propertyName.equals("country") ||
                propertyName.equals("located in the administrative territorial entity") ||
                propertyName.equals("located in time zone"))
              // Debug: for now, skip qualified values.
              continue;
*/
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

    if (valueSet.isEmpty())
      return null;
    else
      return valueSet;
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

  public HashMap<Integer, Item> items_ = new HashMap<>();
  public HashMap<Integer, Property> properties_ = new HashMap<>();

  public interface GetIntArray<T> { int[] getIntArray(T obj); }
  public interface SetIntArray<T> { void setIntArray(T obj, int[] values); }

  public static int QEntity = 35120;
  public static int PinstanceOf = 31;
  public static int PsubclassOf = 279;
  public static int PpartOf = 361;
  public static int Pcountry = 17;
  public static int PlocatedInTheAdministrativeTerritorialEntity = 131;
  public static int PlocatedInTimeZone = 421;
  private static final Gson gson_ = new Gson();
  private static final Pattern itemPattern_ = Pattern.compile
    ("^\\{\"type\":\"item\",\"id\":\"Q(\\d+)");
  private static final Pattern propertyPattern_ = Pattern.compile
    ("^\\{\"type\":\"property\",\"datatype\":\"([\\w-]+)\",\"id\":\"P(\\d+)");
}

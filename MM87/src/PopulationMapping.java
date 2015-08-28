import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 
 */

/**
 * @author d_jash
 * 
 */
public class PopulationMapping
{

  /**
	 * 
	 */
  private static final int TIME_LIMIT = 19000;

  private static Scanner scanner = new Scanner (System.in);

  private static int SUBMISSION_MODE = 0;

  /**
	 * 
	 */
  private static final char LAND_CHAR = 'X';



  public static void main (String[] args)
  {
    try
    {
      int maxPercentage = Integer.parseInt (scanner.nextLine ());

      int H = Integer.parseInt (scanner.nextLine ());
      String[] worldMap = new String[H];
      for (int i = 0; i < H; i++)
      {
        worldMap[i] = scanner.nextLine ();
      }
      int totalPopulation = Integer.parseInt (scanner.nextLine ());

      PopulationMapping mapping = new PopulationMapping ();
      String[] ret = mapping.mapPopulation (maxPercentage, worldMap, totalPopulation);
      System.out.println (ret.length);
      for (int i = 0; i < ret.length; i++)
      {
        System.out.println (ret[i]);
      }
      System.out.flush ();

    }
    catch (Throwable th)
    {
      // System.err.println("ERR :" + th.getMessage());
    }
    finally
    {
      if (scanner != null)
      {
        scanner.close ();
      }
    }
  }



  /**
   * The world, and all the people that live in it, is represented as a
   * rectangular grid, with a certain number of people living in each cell of
   * the grid. For the sake of making pretty maps, I am working on finding areas
   * of the world with population densities of particular interest.
   * 
   * Initially I am given the map of the world's oceans and land in terms of
   * grid units, and the total population of the world. People live only on land
   * cells. Because of the wealth of research that various others have already
   * done, it is possible to determine the population of any rectangular section
   * of the world (without regard to the various densities within that region,
   * or the exact population of any cell included in that region). In your quest
   * to answer this question, you may issue any number of such queries. Of
   * course, answering each query comes with a cost, and thus your goal is to
   * balance the accuracy of your answer against the total number of queries you
   * need to issue.
   * 
   * Your task is to find the largest possible area of the world such that its
   * total population does not exceed a given percentage of the world's total
   * population, using the least amount of queries.
   * 
   * @param maxPercentage
   *          gives you the maximum percentage of the total population that your
   *          selected area may contain.
   * @param worldMap
   *          gives you the world map containing W columns and H rows. Each
   *          String contains a row of the map. Ocean is denoted by the '.'
   *          character and land by the 'X' character.
   * @param totalPopulation
   *          gives you the total population on the entire given world map.
   * @return a String[] containing your selected area. The size of your return
   *         must be the same as the input worldMap. Each String contains a row
   *         of the map. Each cell that you want to select must contain the 'X'
   *         character, otherwise unselected cells must contain the '.'
   *         character.
   * 
   * 
   *         Raw score = (Total land area selected) * (0.996 ^ (Number of
   *         queries))
   * 
   * 
   */
  public String[] mapPopulation (int maxPercentage, String[] worldMap, int totalPopulation)
  {
    long startTime = System.currentTimeMillis ();
    int height = worldMap.length;
    int width = worldMap[0].length ();

    String[] result = new String[height];
    char[][] result_char = new char[height][width];
    // boolean[][] waterMark = new boolean[width][height];

    int[] dx = { -1, -1, 0, 1, 1, 1, 0, -1 };
    int[] dy = { 0, 1, 1, 1, 0, -1, -1, -1 };

    int allowedPopulation = (totalPopulation * maxPercentage) / 100;
    int cumulativePopulation = 0;

    List<Land> landCoordinates = new ArrayList<Land> ();
    Map<Point, Integer> landMap = new HashMap<Point, Integer> ();

    int landCount = 0;

    for (int y = 0; y < height; y++)
    {

      char[] row = worldMap[y].toCharArray ();
      result_char[y] = new char[width];
      for (int x = 0; x < width; x++)
      {

        if (row[x] == LAND_CHAR)
        {

          landCount++;
          Point landPoint = new Point (x, y);

          boolean found = false;
          if (!landMap.isEmpty ())
          {

            for (int i = 0; i < dx.length; i++)
            {
              int neighbour_x = x + dx[i];
              int neighbour_y = y + dy[i];
              if (neighbour_x > -1 && neighbour_y > -1 && neighbour_x < width && neighbour_y < height)
              {
                Point neighbour = new Point (neighbour_x, neighbour_y);
                Integer landIndex = landMap.get (neighbour);
                if (landIndex != null && landIndex > -1)
                {
                  found = true;
                  landCoordinates.get (landIndex).addPoint (landPoint);
                  landMap.put (landPoint, landIndex);
                  break;
                }
              }
            }

          }

          if (!found)
          {
            landMap.put (landPoint, landCoordinates.size ());
            Land land = new Land ();
            land.totalHeight = height;
            land.totalWidth = width;
            land.addPoint (landPoint);
            landCoordinates.add (land);
          }

        }
        else
        {
          result_char[y][x] = '.';
        }
      }

    }

    Collections.sort (landCoordinates);
    int queryCount = 0;
    int queryHit = 0;
    int queryMiss = 0;
    double percentage = 0.00d;
    System.err.println ("query_count,min_x,min_y,max_x,max_y,st_x,st_y,en_x,en_y,land_area,total_area,land_percentage,population,cumulative_population,allowed_population,query_hit,max_percentage");
    for (int i = 0; i < landCoordinates.size (); i++)
    {


      Land eachLand = landCoordinates.get (i);
      double landPercentage = (eachLand.point_count * 100) / landCount;

      int population = 0;

      population = queryRegion (eachLand.min_x, eachLand.min_y, eachLand.max_x, eachLand.max_y);

      cumulativePopulation += population;

      queryCount++;

      int report_cumulative_population = cumulativePopulation;
      boolean querySuccess = false;
      if (cumulativePopulation <= allowedPopulation)
      {

        markRegion (result_char, eachLand.min_x, eachLand.min_y, eachLand.max_x, eachLand.max_y);

        queryHit++;
        percentage = cumulativePopulation * 100 / allowedPopulation;
        querySuccess = true;
      }
      else
      {
        queryMiss++;
        int execssPopulation = cumulativePopulation - allowedPopulation;
        double excessPercentage = execssPopulation * 100 / allowedPopulation;
        cumulativePopulation -= population;
      }

      System.err.println (queryCount + "," + eachLand.min_x + "," + eachLand.min_y + "," + eachLand.max_x + "," + eachLand.max_y + "," + eachLand.start.x + "," + eachLand.start.y + "," + eachLand.end.x + "," + eachLand.end.y + "," + eachLand.point_count + "," + landCount + "," + landPercentage
                          + "," + population + "," + cumulativePopulation + "," + allowedPopulation + "," + querySuccess + "," + maxPercentage);

      if (cumulativePopulation >= allowedPopulation)
      {
        break;
      }
      if (System.currentTimeMillis () - startTime > TIME_LIMIT)
      {
        break;
      }

      if (percentage >= 99.00d)
      {
        break;
      }

    }

    for (int y = 0; y < height; y++)
    {
      result[y] = new String (result_char[y]);
    }
    double successPercentange = queryHit * 100 / queryCount;
    System.err.println ("Total queries :" + queryCount + " queryHit :" + queryHit + " queryMiss " + queryMiss + " successPercentange " + successPercentange + " maxPercentage :" + maxPercentage);
    return result;
  }



  /**
   * @param result_char
   * @param i
   * @param j
   * @param k
   * @param l
   */
  private void markRegion (char[][] result_char, int i, int j, int k, int l)
  {

    for (int y = j; y <= l; y++)
    {
      for (int x = i; x <= k; x++)
      {
        if (result_char[y][x] != '.')
        {
          result_char[y][x] = 'X';
        }
      }
    }

  }



  /**
   * You can call the queryRegion(int x1, int y1, int x2, int y2) method to get
   * the population size within a specific rectangular region. (x1,y1) specifies
   * the lower left corner and (x2,y2) the upper right corner of the rectangular
   * region. Coordinates are inclusive. A row is specified by y1 and y2. A
   * column is specified by x1 and x2. The method will return an int containing
   * the population size within the region. The following rules apply:
   * 
   * x1 <= x2 and y1 <= y2 0 <= x1,x2 < W 0 <= y1,y2 < H
   * 
   * @param x1
   *          lower column
   * @param y1
   *          lower row
   * @param x2
   *          upper column
   * @param y2
   *          upper row
   * @return population size within x1,y1 - x2,y2.
   */
  public static int queryRegion (int x1, int y1, int x2, int y2)
  {

    if (SUBMISSION_MODE == 0)
    {

      System.out.println ("?");
      System.out.println (x1 + " " + y1 + " " + x2 + " " + y2);
      System.out.flush ();
      return Integer.parseInt (scanner.nextLine ());
    }
    else
    {
      return Population.queryRegion (x1, y1, x2, y2);
    }

  }








  static class Land implements Comparable<Land>
  {

    int point_count;

    int min_x = Integer.MAX_VALUE;

    int min_y = Integer.MAX_VALUE;

    int max_x;

    int max_y;

    int totalWidth;

    int totalHeight;

    Point start = null;

    Point end = null;

    List<Point> members = new ArrayList<Point> ();



    public void addPoint (Point point)
    {
      min_x = Math.min (min_x, point.x);
      min_y = Math.min (min_y, point.y);
      max_x = Math.max (max_x, point.x);
      max_y = Math.max (max_y, point.y);
      point_count++;

      if (start == null && end == null)
      {
        start = point;
      }
      else
      {
        end = point;
      }
      members.add (point);
    }



    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo (Land o)
    {
      double size = 0.00d;
      double osize = 0.00d;

     
        size = (max_x - min_x) * (max_y - min_y) + point_count;
//            + getProximityCount () * 0.5;
        osize = (o.max_x - o.min_x) * (o.max_y - o.min_y) + o.point_count;
//        + o.getProximityCount () * 0.5;

        // if (point_count > o.point_count)
        if (size > osize)
        {
          return -1;
        }
        else
          // if (point_count < o.point_count)
          if (size < osize)
          {
            return 1;
          }
      
     
      return 0;
    }



    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
      return "min_x : " + min_x + " , min_y :" + min_y + " , max_x :" + max_x + " , max_y :" + max_y + " ,point_count :" + point_count;
    }



    public int getProximityCount ()
    {
      int count = 0;


      for (Point eachPoint : members)
      {
        int[] dx = { -1, -1, 0, 1, 1, 1, 0, -1 };
        int[] dy = { 0, 1, 1, 1, 0, -1, -1, -1 };

        for (int i = 0; i < dx.length; i++)
        {
          int neighbour_x = eachPoint.x + dx[i];
          int neighbour_y = eachPoint.y + dy[i];
          if (neighbour_x > -1 && neighbour_y > -1 && neighbour_x < totalWidth && neighbour_y < totalHeight)
          {
            Point neighbour = new Point (neighbour_x, neighbour_y);
            if (members.contains (neighbour))
            {
              count++;
            }

          }
        }
      }
      // System.err.println ("PROXIMITY COUNT :" + count);

      return count;

    }
  }
}

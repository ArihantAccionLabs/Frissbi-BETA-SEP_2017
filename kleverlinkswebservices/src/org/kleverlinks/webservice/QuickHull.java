package org.kleverlinks.webservice;

import java.util.ArrayList;
import java.util.Scanner;

import javafx.geometry.Point2D;

public class QuickHull
{
    public ArrayList<Point2D> quickHull(ArrayList<Point2D> Point2Ds)
    {
        ArrayList<Point2D> convexHull = new ArrayList<Point2D>();
        if (Point2Ds.size() < 3)
            return (ArrayList) Point2Ds.clone();
 
        int minPoint2D = -1, maxPoint2D = -1;
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        for (int i = 0; i < Point2Ds.size(); i++)
        {
            if (Point2Ds.get(i).getX() < minX)
            {
                minX = Point2Ds.get(i).getX();
                minPoint2D = i;
            }
            if (Point2Ds.get(i).getX() > maxX)
            {
                maxX = Point2Ds.get(i).getX();
                maxPoint2D = i;
            }
        }
        Point2D A = Point2Ds.get(minPoint2D);
        Point2D B = Point2Ds.get(maxPoint2D);
        convexHull.add(A);
        convexHull.add(B);
        Point2Ds.remove(A);
        Point2Ds.remove(B);
 
        ArrayList<Point2D> leftSet = new ArrayList<Point2D>();
        ArrayList<Point2D> rightSet = new ArrayList<Point2D>();
 
        for (int i = 0; i < Point2Ds.size(); i++)
        {
        	Point2D p = Point2Ds.get(i);
            if (Point2DLocation(A, B, p) == -1)
                leftSet.add(p);
            else if (Point2DLocation(A, B, p) == 1)
                rightSet.add(p);
        }
        hullSet(A, B, rightSet, convexHull);
        hullSet(B, A, leftSet, convexHull);
 
        return convexHull;
    }
 
    public double distance(Point2D A, Point2D B, Point2D C)
    {
        double ABx = B.getX() - A.getX();
        double ABy = B.getY() - A.getY();
        double num = ABx * (A.getY() - C.getY()) - ABy * (A.getX() - C.getX());
        if (num < 0)
            num = -num;
        return num;
    }
 
    public void hullSet(Point2D A, Point2D B, ArrayList<Point2D> set,
            ArrayList<Point2D> hull)
    {
        int insertPosition = hull.indexOf(B);
        if (set.size() == 0)
            return;
        if (set.size() == 1)
        {
            Point2D p = set.get(0);
            set.remove(p);
            hull.add(insertPosition, p);
            return;
        }
        double dist = Double.MIN_VALUE;
        int furthestPoint2D = -1;
        for (int i = 0; i < set.size(); i++)
        {
            Point2D p = set.get(i);
            double distance = distance(A, B, p);
            if (distance > dist)
            {
                dist = distance;
                furthestPoint2D = i;
            }
        }
        Point2D P = set.get(furthestPoint2D);
        set.remove(furthestPoint2D);
        hull.add(insertPosition, P);
 
        // Determine who's to the left of AP
        ArrayList<Point2D> leftSetAP = new ArrayList<Point2D>();
        for (int i = 0; i < set.size(); i++)
        {
            Point2D M = set.get(i);
            if (Point2DLocation(A, P, M) == 1)
            {
                leftSetAP.add(M);
            }
        }
 
        // Determine who's to the left of PB
        ArrayList<Point2D> leftSetPB = new ArrayList<Point2D>();
        for (int i = 0; i < set.size(); i++)
        {
            Point2D M = set.get(i);
            if (Point2DLocation(P, B, M) == 1)
            {
                leftSetPB.add(M);
            }
        }
        hullSet(A, P, leftSetAP, hull);
        hullSet(P, B, leftSetPB, hull);
 
    }
 
    public int Point2DLocation(Point2D A, Point2D B, Point2D P)
    {
        double cp1 = (B.getX() - A.getX()) * (P.getY() - A.getY()) - (B.getY() - A.getY()) * (P.getX() - A.getX());
        if (cp1 > 0)
            return 1;
        else if (cp1 == 0)
            return 0;
        else
            return -1;
    }
    
    public Point2D findCentroid(ArrayList<Point2D> Point2Ds){
    	Point2D point2d = null;
    	double x = 0.0;
    	double y = 0.0;
    	
    	for (int i = 0; i < Point2Ds.size(); i++)
        {
            x+=Point2Ds.get(i).getX();
            y+=Point2Ds.get(i).getY();
        }
    	x/=  Point2Ds.size();
    	y/=  Point2Ds.size();
    	point2d = new Point2D(x, y);
    	return point2d;
    }
 
    public static void main(String args[])
    {
        System.out.println("Quick Hull Test");
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter the number of Point2Ds");
        int N = sc.nextInt();
 
        ArrayList<Point2D> Point2Ds = new ArrayList<Point2D>();
        System.out.println("Enter the coordinates of each Point2Ds: <x> <y>");
        for (int i = 0; i < N; i++)
        {
            double x = sc.nextDouble();
            double y = sc.nextDouble();
            Point2D e = new Point2D(x, y);
            Point2Ds.add(i, e);
        }
 
        QuickHull qh = new QuickHull();
        ArrayList<Point2D> p = qh.quickHull(Point2Ds);
        System.out
                .println("The Points in the Convex hull using Quick Hull are: ");
        for (int i = 0; i < p.size(); i++)
            System.out.println("(" + p.get(i).getX() + ", " + p.get(i).getY() + ")");
        sc.close();
    }
}

rem for /1 %x in (1, 1, 10) do echo ( %%x )
rem java -jar lib\tester.jar -exec "java -classpath bin PopulationMapping" -seed %1 -novis
for /l %x in (1, 1, 100) do echo %x
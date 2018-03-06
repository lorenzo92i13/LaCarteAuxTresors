package com.laurent.manager;

import com.google.common.collect.Lists;
import com.laurent.domain.Adventurer;
import com.laurent.domain.Case;
import com.laurent.domain.Orientation;
import com.laurent.domain.Position;
import com.laurent.domain.TreasureMap;
import com.laurent.domain.Treasures;
import com.laurent.factory.AdventurersFactory;
import com.laurent.factory.MountainFactory;
import com.laurent.factory.TreasureFactory;
import com.laurent.factory.TreasureMapFactory;

import java.util.List;

public final class GameManager {

    public static void LaunchGame()
    {
        final List<List<String>> lines = LinesRepository.getLines(IOFile.readFile(),
                                                                  " - ");

        final List<Adventurer> adventurers = Lists.newArrayList();

        final List<Case> cases = Lists.newArrayList();

        final List<TreasureMap> treasureMaps = Lists.newArrayList();

        lines.forEach(line -> {
            switch (line.get(0)) {
                case "C":
                    treasureMaps.add(TreasureMapFactory.createMap(line));
                    break;
                case "A":
                    adventurers.add(AdventurersFactory.createAdventurers(line));
                    break;
                case "T":
                    cases.add(TreasureFactory.createTreasure(line));
                    break;
                case "M":
                    cases.add(MountainFactory.createMountain(line));
                    break;
                default:
                    break;
            }
        });

        final TreasureMap treasureMap = treasureMaps.get(0);

        cases.forEach(treasureMap::addObject);

        readSequence(treasureMap, adventurers);
    }

    private static void readSequence(final TreasureMap treasureMap, final List<Adventurer> adventurers)
    {
        for (final Adventurer adventurer : adventurers) {

            final Position currentPosition = adventurer.getPositions().get(adventurer.getPositions().size() - 1);

            pickUpIfExistTreasure(treasureMap, adventurer);

            for (int i = 0; i < adventurer.getMovements().size(); i++) {
                final Orientation currentOrientation = adventurer.getOrientations().get(adventurer.getOrientations().size() - 1);
                final Position futurePosition = MovementResolver.moveForward(currentOrientation, currentPosition);

                switch (adventurer.getMovements().get(i)) {
                    case A:
                        if (isMovable(treasureMap, adventurers, futurePosition)) {
                            moveForward(adventurer, currentPosition, currentOrientation);
                            pickUpIfExistTreasure(treasureMap, adventurer);
                        }
                        break;
                    case G:
                        adventurer.addOrientation(MovementResolver.moveLeft(currentOrientation));
                        break;
                    case D:
                        adventurer.addOrientation(MovementResolver.moveRight(currentOrientation));
                        break;
                    default:
                        break;
                }
            }
        }
        FileHelper.writeInFile(treasureMap, adventurers);
    }

    private static void moveForward(final Adventurer adventurer, final Position currentPosition, final Orientation currentOrientation)
    {
        final Position position = MovementResolver.moveForward(currentOrientation,
                                                               currentPosition);

        adventurer.addPosition(position);
    }

    private static void pickUpIfExistTreasure(final TreasureMap treasureMap, final Adventurer adventurer)
    {
        final Position position = adventurer.getPositions().get(adventurer.getPositions().size() - 1);
        if (treasureMap.getCase(position.getX(), position.getY()) instanceof Treasures) {
            final Treasures treasures = (Treasures) treasureMap.getCase(position.getX(), position.getY());

            treasures.removeTreasure();
            adventurer.addTreasure();
        }
    }

    private static boolean isMovable(final TreasureMap treasureMap, final List<Adventurer> adventurers, final Position position)
    {
        return position.getX() >= 0 && position.getY() >= 0 &&
               position.getX() < treasureMap.getWidth() &&
               position.getY() < treasureMap.getLength() &&
               treasureMap.getCase(position.getX(), position.getY()).isPassable() &&
               !adventurers.contains(position);
    }

    private GameManager()
    {
        // NOP
    }
}
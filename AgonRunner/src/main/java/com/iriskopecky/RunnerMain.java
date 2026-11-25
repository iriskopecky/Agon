package com.iriskopecky;

import com.iriskopecky.util.*;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class RunnerMain {


    public static void main(String[] args) throws Exception {
        //make comms bridge
        PrintStream out = System.out;
        System.setOut(System.err); //ensure contestants cannot message to our comms bridge.
        JsonMapper mapper = JsonMapper.builder().build();
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        //get the teras class
        String terasClassName = args[0];
        Class<?> unknownClass = Class.forName(terasClassName);
        Class<? extends PlayerTeras> terasClass = unknownClass.asSubclass(PlayerTeras.class);

        //prep our internal memory of the terata associated with this runner.
        HashMap<String, PlayerTeras> terasInstances = new HashMap<>();

        //read messages
        String readLine;
        while ((readLine = input.readLine()) != null) {
            Message message = mapper.readValue(readLine, Message.class);
            Response response = null;

            //parse message and act
            switch (message.command()) {
                case MAKE: {
                    PlayerTeras teras = terasClass.getDeclaredConstructor().newInstance();
                    terasInstances.put(message.target(), teras);
                    break;
                }
                case INIT: {
                    InitialState state = mapper.convertValue(message.data(), InitialState.class);
                    if (terasInstances.containsKey(message.target())) {
                        PlayerTeras teras = terasInstances.get(message.target());
                        teras.initialize(state);
                    }
                    break;
                }
                case SCAN: {
                    Scan scan = mapper.convertValue(message.data(), Scan.class);
                    if (terasInstances.containsKey(message.target())) {
                        PlayerTeras teras = terasInstances.get(message.target());
                        teras.scanResults(scan);
                    }
                    break;
                }
                case DECIDE_ACTIONS: {
                    if (terasInstances.containsKey(message.target())) {
                        PlayerTeras teras = terasInstances.get(message.target());
                        ActionPair actions = teras.decideActions();
                        response = new Response(message.target(), Response.ResponseType.ACTION_DECISION, actions);
                    }
                    break;
                }
                case ATTACK: {
                    TerasView defender = mapper.convertValue(message.data(), TerasView.class);
                    if (terasInstances.containsKey(message.target())) {
                        PlayerTeras teras = terasInstances.get(message.target());
                        Attack attack = teras.attack(defender);
                        response = new Response(message.target(), Response.ResponseType.ATTACK, attack);
                    }
                    break;
                }
                case DEFEND: {
                    TerasView attacker = mapper.convertValue(message.data(), TerasView.class);
                    if (terasInstances.containsKey(message.target())) {
                        PlayerTeras teras = terasInstances.get(message.target());
                        Defense defense = teras.defend(attacker);
                        response = new Response(message.target(), Response.ResponseType.DEFENSE, defense);
                    }
                    break;
                }
                case FIGHT_RESULT: {
                    FightResult result = mapper.convertValue(message.data(), FightResult.class);
                    if (terasInstances.containsKey(message.target())) {
                        PlayerTeras teras = terasInstances.get(message.target());
                        teras.fightResult(result);
                    }
                    break;
                }
                case SPEND_RESOURCES: {
                    Pair<UpgradeState, PortentState> statePair = mapper.convertValue(message.data(), new TypeReference<>() {});
                    if (terasInstances.containsKey(message.target())) {
                        PlayerTeras teras = terasInstances.get(message.target());
                        Purchase purchase = teras.spendResources(statePair.first(), statePair.second());
                        response = new Response(message.target(), Response.ResponseType.PURCHASE, purchase);
                    }
                    break;
                }
                case SHOULD_MATE: {
                    Pair<TerasView, PortentState> statePair = mapper.convertValue(message.data(), new TypeReference<>() {});
                    if (terasInstances.containsKey(message.target())) {
                        PlayerTeras teras = terasInstances.get(message.target());
                        boolean shouldMate = teras.shouldMate(statePair.first(), statePair.second());
                        response = new Response(message.target(), Response.ResponseType.WANTS_TO_MATE, shouldMate);
                    }
                    break;
                }
                case MOVE: {
                    if (terasInstances.containsKey(message.target())) {
                        PlayerTeras teras = terasInstances.get(message.target());
                        Move move = teras.move();
                        response = new Response(message.target(), Response.ResponseType.MOVE, move);
                    }
                    break;
                }
                case TICK_END_STATE: {
                    TickEndState state = mapper.convertValue(message.data(), TickEndState.class);
                    if (terasInstances.containsKey(message.target())) {
                        PlayerTeras teras = terasInstances.get(message.target());
                        teras.tickEndState(state);
                    }
                    break;
                }
                default: {
                    response = new Response(message.target(), Response.ResponseType.ERROR, null);
                }
            }

            //send response
            if (response == null) {
                response = new Response(message.target(), Response.ResponseType.OK, null);
            }

            out.println(mapper.writeValueAsString(response));
            out.flush();
        }
    }
}

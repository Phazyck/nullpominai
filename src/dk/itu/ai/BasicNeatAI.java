package dk.itu.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.anji.integration.Activator;

import mu.nu.nullpo.game.component.Block;
import mu.nu.nullpo.game.component.Controller;
import mu.nu.nullpo.game.component.Field;
import mu.nu.nullpo.game.component.Piece;
import mu.nu.nullpo.game.play.GameEngine;
import mu.nu.nullpo.game.subsystem.ai.BasicAI;

public class BasicNeatAI extends BasicAI {
	
	// Neural Network activation object
	private Activator networkActivator;
	
	public BasicNeatAI(Activator ac) {
		networkActivator = ac;
	}
	
	// TODO(Kasra): Get a collection of different available moves
	// TODO(Kasra): Pass each of these moves to the activator, get an associated score
	// TODO(Kasra): Pick highest scored move, set fields to let the BasicAI funcs navigate to that pos
}

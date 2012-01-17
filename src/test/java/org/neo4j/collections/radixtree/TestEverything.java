/**
 * Copyright (c) 2002-2012 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.collections.radixtree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;
import org.neo4j.graphdb.Node;


public class TestEverything extends RadixTreeTestCase {
	
	@Test
	public void testEverything() throws Exception {
		RadixTree index = createIndex();
		
		insertAndGet(index, new String[] {
			"romane", 
			"caesar", 
			"romanus", 
			"rubens", 
			"ruber", 
			"rubicon", 
			"rubicundus",
			"romulus" });
		
		debugIndexTree(index, graphDb().getReferenceNode());  

		String[] users = new String[] {
				"RoadGeek_MD99",
				"szhorvat",
				"theophrastos",
				"emvee",
				"wuerfel",
				"Phil Shipley",
				"xylome",
				"Hartmut Holzgraefe",
				"Nabada",
				"revert_glaucos",		
				"schemb",
				"user_5359",
				"Pmz",
				"gakki007",
				"Talors",
				"Raphael Borg Ellul Vincenti",
				"raymio",
				"Althalus",
				"Rocksailor",
				"Hitchhiker79",
				"Daeron",
				"Kevin Carbonaro",
				"markusw",
				"cojo",
				"Joe Bonnici",
				"magpie74",
				"pli",
				"jdecelis",
				"GarySmith",
				"mapper_07",
				"navmaps_eu",
				"elbatrop",
				"mcknut",
				"mdeb",
				"Giulio Camilleri - 115 The Strand Aparthotel",		
				"malcolmh",
				"listoflights",
				"ixprun",
				"nadzri",
				"totoko",
				"pieleric",
				"wmann",
				"WalterH",
				"babsetta",
				"adjuva",
				"Mark40",
				"stefmif",
				"cuu508",
				"Netzwolf",
				"mr_road",
				"Steve Bennett",		
				"Djam",
				"wieland",
				"trancefusion",
				"meeee",
				"zenfunk",
				"An Goban Saor",
				"SteveC",
				"ilBahri",
				"JcMalta",
				"mwab",
				"alarants",
				"gschneider",
				"njay",
				"peeteepee",
				"mstriewe",
				"Tric",
				"lange-chemnitz",
				"tixuwuoz",
				"csdf",
				"pjotr",
				"maximeguillaud",
				"lmk",
				"dforsi",
				"ike88",
				"MartinDornfelder",
				"sakaAl",
				"don-vip",
				"hermann_sl",
				"Victor Forte",
				"Magnabear",
				"Claudius Henrichs",
				"Kollege Moevenpick",
				"LukasM",
				"DavBro",
				"uboot",
				"cgu66",
				"ndani",
				"nimsk",
				"wheelmap_visitor",
				"Martin_D",
				"HermannSchwaerzler",
				"Manchito",
				"quarksteilchen",
				"schmucky",
				"malenki",
				"Tyrone Slothrop",
				"delfin_6",
				"Jesper Mortensen",
				"MariaAgius",
				"xybot",
				"afs",
				"desilex",
				"cilugnedon",
				"TheMapper",
				"KHugz",
				"Fassy",
				"neufeind",
				"Flacus",
				"++ Lex ++",
				"StefanTiran",
				"Ropino",
				"raqui",
				"chrisdarmanin",
				"joseph bonnici",
				"jolly47roger",
				"awdjo",
				"Tremlin",
				"Mathiasen Paul",
				"cass1",
				"riechfield",
				"Daniel77C",
				"svetico",
				"Mike Aubury",
				"joe borg",
				"gunglien",
				"reubenbubu",
				"Adrian Camilleri",
				"Mario Sammut",
				"Johansson",
				"jgui",
				"Avel2",
				"Darren Mizzi",
				"shikaku",
				"grg183",
				"Yowzef",
				"Marc Schütz",
				"NE2",
				"JohnSmith",
				"Red Dwarf Planet",
				"mdmello1958",
				"Kejiro",
				"9hbr",
				"M2B",
				"gfspiteri",
				"magmer",
				"benchMark",
				"J J",
				"staehler",
				"Walter Schlögl",
				"t-i",
				"Eric Pace",
				"mandan",
				"brucebb",
				"stoschek",
				"jaspis",
				"Grech",
				"PeterM",
				"ghamos",
				"FabianS",
				"Tony Sammut",
				"Peter14",
				"Xmun",
				"Bowyer House",
				"Xtomi",
				"glennzarb",
				"vbu",
				"moritzh",
				"aamorett",
				"Snoopy88",
				"Christopher Bartolo",
				"NCami",
				"chdr",
				"OmEnMT",
				"marzo",
				"kayowdi",
				"gaffa",
				"wambacher",
				"Andre68",
				"Glenn Sciortino",
				"ulfl",
				"clgowa",
				"nevcas",
				"lyx",
				"peter88823",
				"osm191639",
				"zimbo",
				"michael246",
				"oha",
				"Damian Allison",
				"Pinu",
				"nikkinikki",
				"narcisa",
				"Anthony J",
				"IknowJoseph",
				"madcossie",
				"Ian----",
				"kbriffa",
				"BiIbo",
				"Ramzes",
				"XPhiler",
				"Oceanic",
				"mikes",
				"HiVoltage",
				"guenter",
				"ZMWandelaar",
				"ChrisMerc",
				"Janu",
				"mappa",
				"AChoice",
				"mabapla",
				"schoschi",
				"Eratosthenes",
				"angzam78",
				"El23",
				"Maarten Deen",
				"lyrie",
				"dmgroom",
				"hinste",
				"Tronikon",
				"It's so funny",
				"guggis",
				"mikefalzon",
				"George Vella",
				"nodilution",
				"Trumbun",
				"GeoGrafiker" };
		
		insertAndGet(index, users);
		
		
		// insert again and check 2 values are present
		
		for (String user : users) {
			index(index, user);			
		}		
		
		for (String user : users) {
			List<Node> nodes = index.get(user);
			assertEquals(2, nodes.size(), 0);
			
			for (Node node : nodes) {
				assertEquals(user, node.getProperty("label"));
			}
		}
		
		
		// insert again
		
		for (String user : users) {
			assertFalse(indexUnique(index, user));
		}
		
		
		// remove
		
		for (String user : users) {
			assertEquals(2, index.remove(user, true));
		}
		
		for (String user : users) {
			assertEquals(0, index.get(user).size());
		}
    }
	
	private void insertAndGet(RadixTree index, String[] values) {
		for (String value : values) {
			index(index, value);			
		}
		
		for (String value : values) {
			get(index, value);
		}
	}
	
	private void get(RadixTree index, String key) {
		assertEquals(key, (String) index.getUnique(key).getProperty("label"));		
	}
	
	private void index(RadixTree index, String key) {
		Node node = createSampleNode(key);
		index.insert(key, node);
	}

	private boolean indexUnique(RadixTree index, String key) {
		Node node = createSampleNode(key);
		return index.insertUnique(key, node);
	}	
	
}
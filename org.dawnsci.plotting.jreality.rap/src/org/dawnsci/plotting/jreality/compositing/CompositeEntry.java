/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.jreality.compositing;


/**
 *
 */
public class CompositeEntry {

	private String name;
	private float weight;
	private CompositeOp operation;
	private byte channelMask;
	
	public CompositeEntry(String name, float weight, CompositeOp op,
						  byte channelMask) {
		this.name = name;
		this.weight = weight;
		this.operation = op;
		this.channelMask = channelMask;
	}
	
	public final String getName() {
		return name;
	}
	
	public final float getWeight() {
		return weight;
	}
	
	public final CompositeOp getOperation() {
		return operation;
	}
	
	public final byte getChannelMask() {
		return channelMask;
	}
}

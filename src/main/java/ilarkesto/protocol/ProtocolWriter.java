/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package ilarkesto.protocol;

import ilarkesto.protocol.Message.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ProtocolWriter {

	private List<AProtocolConsumer> consumers = new ArrayList<AProtocolConsumer>();

	private Stack<String> contextStack = new Stack<String>();

	public ProtocolWriter(AProtocolConsumer... consumers) {
		addConsumers(consumers);
		if (this.consumers.isEmpty()) this.consumers.add(new SysoutProtocolConsumer());
	}

	public ProtocolWriter addConsumers(AProtocolConsumer... consumers) {
		for (AProtocolConsumer consumer : consumers) {
			this.consumers.add(consumer);
		}
		return this;
	}

	public void pushContext(String context) {
		for (AProtocolConsumer consumer : consumers) {
			consumer.onContextStart(context, contextStack);
		}
		contextStack.push(context);
	}

	public void popContext() {
		String context = contextStack.pop();
		for (AProtocolConsumer consumer : consumers) {
			consumer.onContextEnd(context, contextStack);
		}
	}

	public void info(Object... message) {
		message(new Message(Type.INFO, message));
	}

	public void error(Object... message) {
		message(new Message(Type.ERROR, message));
	}

	private void message(Message message) {
		for (AProtocolConsumer consumer : consumers) {
			consumer.onMessage(message, contextStack);
		}
	}

}

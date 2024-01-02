/*
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

package ai.datamaker.exception;

import ai.datamaker.model.Dataset;

/**
 * Thrown during serialization of a {@link Dataset}.
 */
public class DatasetSerializationException extends RuntimeException {

    private final Dataset dataset;

    public DatasetSerializationException(String message, Throwable cause, Dataset dataset) {
        super(message + " for dataset: " + String.format("workspace=%s, name=%s, id=%s",
                                                         dataset.getWorkspace() != null ? dataset.getWorkspace().getName() : null,
                                                         dataset.getName(),
                                                         dataset.getExternalId()),
              cause);
        this.dataset = dataset;
    }

}

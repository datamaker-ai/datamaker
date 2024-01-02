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

package ai.datamaker.repository.listener;

import ai.datamaker.model.field.FieldMapping;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

@Slf4j
public class FieldMappingListener {

    @PrePersist
    @PreUpdate
    @PreRemove
    private void beforeAnyUpdate(FieldMapping fieldMapping) {
        if (fieldMapping.getId() == 0) {
            log.info("[FIELD_MAPPING AUDIT] About to add a field mapping");
        } else {
            log.info("[FIELD_MAPPING AUDIT] About to update/delete field mapping: " + fieldMapping.getId());
        }
    }

    @PostUpdate
    private void afterAnyUpdate(FieldMapping fieldMapping) {
        log.info("[FIELD_MAPPING AUDIT] add/update/delete complete for field mapping: " + fieldMapping.getId());
    }
    
    @PostLoad
    private void afterLoad(FieldMapping fieldMapping) {
        log.info("[FIELD_MAPPING AUDIT] field mapping loaded from database: " + fieldMapping.getId());
    }
}
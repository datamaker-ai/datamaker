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

package ai.datamaker.utils.model;

import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldConfig;
import ai.datamaker.model.forms.FieldForm;

import java.util.Locale;

/**
 * Create field from form.
 */
public final class FieldFactory {

    public static Field createField(FieldForm fieldForm) throws Exception {
        Field field = getField(
                        fieldForm.getClassName().contains("-") ?
                            fieldForm.getClassName().substring(0, fieldForm.getClassName().indexOf('-')) :
                            fieldForm.getClassName(),
                        fieldForm.getName(),
                        Locale.forLanguageTag(fieldForm.getLanguageTag()));

        field.setDescription(fieldForm.getDescription());
        field.setIsNullable(fieldForm.getIsNullable());
        field.setIsNested(fieldForm.getIsNested());
        field.setFormatterClassName(fieldForm.getFormatterClassName());
        FieldConfig config = new FieldConfig();
        config.putAll(fieldForm.getConfig());
        field.setConfig(config);
        field.setIsAttribute(fieldForm.getIsAttribute());
        field.setIsAlias(fieldForm.getIsAlias());
        // field.setFormatter();
        field.setIsPrimaryKey(fieldForm.getIsPrimaryKey());
        field.setPosition(fieldForm.getPosition());

        return  field;
    }

    private static Field getField(String className, String name, Locale locale) throws Exception {
        Class<?> fieldClass = Class.forName(className);

        try {
            return (Field) fieldClass.getDeclaredConstructor(String.class, Locale.class)
                                     .newInstance(name, locale);
        } catch (NoSuchMethodException e) {
            Field field = (Field) fieldClass.getDeclaredConstructor().newInstance();
            field.setName(name);
            field.setLocale(locale);
            return field;
        }
    }
}

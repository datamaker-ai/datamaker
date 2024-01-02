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

package ai.datamaker.utils;

import ai.datamaker.model.CustomUserDetails;
import ai.datamaker.model.User;
import ai.datamaker.model.UserGroup;
import com.google.common.collect.Sets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    public static final UserGroup USER_GROUP = new UserGroup();
    static {
        USER_GROUP.setName("test_group");
    }

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User user = new User();
        user.setUsername(customUser.username());
        user.setPassword("12345");
        user.setFirstName(customUser.firstName());
        user.setLastName(customUser.lastName());
        user.setExternalId(UUID.fromString("d86f86f0-281d-11ea-978f-2e728ce88124"));
        user.setId(5L);
        user.setEnabled(true);
        LocalDateTime localDateTime = LocalDateTime.of(2008, 8, 8, 8, 8, 8);
        user.setDateCreated(Timestamp.valueOf(localDateTime));
        user.setLocale(Locale.CANADA);

        UserGroup userGroup = new UserGroup();
        userGroup.setExternalId(UUID.fromString("d86f8326-281d-11ea-978f-2e728ce88123"));
        userGroup.setId(3L);
        userGroup.setName("test-readonly");
        userGroup.setDescription("");

        user.setGroups(Sets.newHashSet(userGroup));

        CustomUserDetails principal = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
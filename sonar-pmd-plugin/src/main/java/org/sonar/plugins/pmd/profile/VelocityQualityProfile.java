/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.plugins.pmd.profile;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.plugins.pmd.PmdConstants;
import org.sonar.plugins.pmd.language.VelocityLanguage;
import org.sonar.plugins.pmd.rule.AliRulesDefinition;

/**
 * 描述：<br>
 * 版权：Copyright (c) 2019<br>
 * 公司：北京活力天汇<br>
 * 作者：陆思骏<br>
 * 版本：1.0<br>
 * 创建日期：2019-10-23<br>
 *
 * @author 陆思骏
 */
public class VelocityQualityProfile implements BuiltInQualityProfilesDefinition {

    @Override
    public void define(Context context) {
        NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile("P3C", VelocityLanguage.KEY);
        profile.setDefault(true);

        for (String ruleKey : AliRulesDefinition.getVelocityRules()) {
            profile.activateRule(PmdConstants.REPOSITORY_P3C_VM_KEY, ruleKey);
        }

        profile.done();
    }

}

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

package org.sonar.plugins.pmd.language;

import org.sonar.api.resources.AbstractLanguage;

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
public class VelocityLanguage extends AbstractLanguage {

    public static final String KEY = "vm";
    private static final String NAME = "Velocity";
    private static final String FILE_SUFFIX = ".vm";

    public VelocityLanguage() {
        super(KEY, NAME);
    }

    @Override
    public String[] getFileSuffixes() {
        return new String[]{FILE_SUFFIX};
    }
}

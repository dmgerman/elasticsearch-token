begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.util.settings
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|SizeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|TimeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadSafe
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Immutable settings allowing to control the configuration.  *  * @author kimchy (Shay Banon)  * @see ImmutableSettings  */
end_comment

begin_interface
annotation|@
name|ThreadSafe
DECL|interface|Settings
specifier|public
interface|interface
name|Settings
block|{
comment|/**      * The global settings if these settings are group settings.      */
DECL|method|getGlobalSettings
name|Settings
name|getGlobalSettings
parameter_list|()
function_decl|;
comment|/**      * Component settings for a specific component. Returns all the settings for the given class, where the      * FQN of the class is used, without the<tt>org.elasticsearch<tt> prefix.      */
DECL|method|getComponentSettings
name|Settings
name|getComponentSettings
parameter_list|(
name|Class
name|component
parameter_list|)
function_decl|;
comment|/**      * Component settings for a specific component. Returns all the settings for the given class, where the      * FQN of the class is used, without provided prefix.      */
DECL|method|getComponentSettings
name|Settings
name|getComponentSettings
parameter_list|(
name|String
name|prefix
parameter_list|,
name|Class
name|component
parameter_list|)
function_decl|;
comment|/**      * The class loader associted with this settings.      */
DECL|method|getClassLoader
name|ClassLoader
name|getClassLoader
parameter_list|()
function_decl|;
comment|/**      * The settings as a {@link java.util.Map}.      */
DECL|method|getAsMap
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|getAsMap
parameter_list|()
function_decl|;
comment|/**      * Returns the setting value associated with the setting key.      *      * @param setting The setting key      * @return The setting value,<tt>null</tt> if it does not exists.      */
DECL|method|get
name|String
name|get
parameter_list|(
name|String
name|setting
parameter_list|)
function_decl|;
comment|/**      * Returns the setting value associated with the setting key. If it does not exists,      * returns the default value provided.      *      * @param setting      The setting key      * @param defaultValue The value to return if no value is associated with the setting      * @return The setting value, or the default value if no value exists      */
DECL|method|get
name|String
name|get
parameter_list|(
name|String
name|setting
parameter_list|,
name|String
name|defaultValue
parameter_list|)
function_decl|;
comment|/**      * Returns group settings for the given setting prefix.      */
DECL|method|getGroups
name|Map
argument_list|<
name|String
argument_list|,
name|Settings
argument_list|>
name|getGroups
parameter_list|(
name|String
name|settingPrefix
parameter_list|)
throws|throws
name|SettingsException
function_decl|;
comment|/**      * Returns the setting value (as float) associated with the setting key. If it does not exists,      * returns the default value provided.      *      * @param setting      The setting key      * @param defaultValue The value to return if no value is associated with the setting      * @return The (float) value, or the default value if no value exists.      * @throws SettingsException Failure to parse the setting      */
DECL|method|getAsFloat
name|Float
name|getAsFloat
parameter_list|(
name|String
name|setting
parameter_list|,
name|Float
name|defaultValue
parameter_list|)
throws|throws
name|SettingsException
function_decl|;
comment|/**      * Returns the setting value (as double) associated with the setting key. If it does not exists,      * returns the default value provided.      *      * @param setting      The setting key      * @param defaultValue The value to return if no value is associated with the setting      * @return The (double) value, or the default value if no value exists.      * @throws SettingsException Failure to parse the setting      */
DECL|method|getAsDouble
name|Double
name|getAsDouble
parameter_list|(
name|String
name|setting
parameter_list|,
name|Double
name|defaultValue
parameter_list|)
throws|throws
name|SettingsException
function_decl|;
comment|/**      * Returns the setting value (as int) associated with the setting key. If it does not exists,      * returns the default value provided.      *      * @param setting      The setting key      * @param defaultValue The value to return if no value is associated with the setting      * @return The (int) value, or the default value if no value exists.      * @throws SettingsException Failure to parse the setting      */
DECL|method|getAsInt
name|Integer
name|getAsInt
parameter_list|(
name|String
name|setting
parameter_list|,
name|Integer
name|defaultValue
parameter_list|)
throws|throws
name|SettingsException
function_decl|;
comment|/**      * Returns the setting value (as long) associated with the setting key. If it does not exists,      * returns the default value provided.      *      * @param setting      The setting key      * @param defaultValue The value to return if no value is associated with the setting      * @return The (long) value, or the default value if no value exists.      * @throws SettingsException Failure to parse the setting      */
DECL|method|getAsLong
name|Long
name|getAsLong
parameter_list|(
name|String
name|setting
parameter_list|,
name|Long
name|defaultValue
parameter_list|)
throws|throws
name|SettingsException
function_decl|;
comment|/**      * Returns the setting value (as boolean) associated with the setting key. If it does not exists,      * returns the default value provided.      *      * @param setting      The setting key      * @param defaultValue The value to return if no value is associated with the setting      * @return The (boolean) value, or the default value if no value exists.      * @throws SettingsException Failure to parse the setting      */
DECL|method|getAsBoolean
name|Boolean
name|getAsBoolean
parameter_list|(
name|String
name|setting
parameter_list|,
name|Boolean
name|defaultValue
parameter_list|)
throws|throws
name|SettingsException
function_decl|;
comment|/**      * Returns the setting value (as time) associated with the setting key. If it does not exists,      * returns the default value provided.      *      * @param setting      The setting key      * @param defaultValue The value to return if no value is associated with the setting      * @return The (time) value, or the default value if no value exists.      * @throws SettingsException Failure to parse the setting      * @see TimeValue#parseTimeValue(String, org.elasticsearch.util.TimeValue)      */
DECL|method|getAsTime
name|TimeValue
name|getAsTime
parameter_list|(
name|String
name|setting
parameter_list|,
name|TimeValue
name|defaultValue
parameter_list|)
throws|throws
name|SettingsException
function_decl|;
comment|/**      * Returns the setting value (as size) associated with the setting key. If it does not exists,      * returns the default value provided.      *      * @param setting      The setting key      * @param defaultValue The value to return if no value is associated with the setting      * @return The (size) value, or the default value if no value exists.      * @throws SettingsException Failure to parse the setting      * @see SizeValue#parse(String, SizeValue)      */
DECL|method|getAsSize
name|SizeValue
name|getAsSize
parameter_list|(
name|String
name|setting
parameter_list|,
name|SizeValue
name|defaultValue
parameter_list|)
throws|throws
name|SettingsException
function_decl|;
comment|/**      * Returns the setting value (as a class) associated with the setting key. If it does not exists,      * returns the default class provided.      *      * @param setting      The setting key      * @param defaultClazz The class to return if no value is associated with the setting      * @param<T>          The type of the class      * @return The class setting value, or the default class provided is no value exists      * @throws NoClassSettingsException Failure to load a class      */
DECL|method|getAsClass
parameter_list|<
name|T
parameter_list|>
name|Class
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|getAsClass
parameter_list|(
name|String
name|setting
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|defaultClazz
parameter_list|)
throws|throws
name|NoClassSettingsException
function_decl|;
comment|/**      * Returns the setting value (as a class) associated with the setting key. If the value itself fails to      * represent a loadable class, the value will be appended to the<tt>prefixPackage</tt> and suffixed with the      *<tt>suffixClassName</tt> and it will try to be loaded with it.      *      * @param setting         The setting key      * @param defaultClazz    The class to return if no value is associated with the setting      * @param prefixPackage   The prefix package to prefix the value with if failing to load the class as is      * @param suffixClassName The suffix class name to prefix the value with if failing to load the class as is      * @param<T>             The type of the class      * @return The class represented by the setting value, or the default class provided if no value exists      * @throws NoClassSettingsException Failure to load the class      */
DECL|method|getAsClass
parameter_list|<
name|T
parameter_list|>
name|Class
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|getAsClass
parameter_list|(
name|String
name|setting
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|T
argument_list|>
name|defaultClazz
parameter_list|,
name|String
name|prefixPackage
parameter_list|,
name|String
name|suffixClassName
parameter_list|)
throws|throws
name|NoClassSettingsException
function_decl|;
comment|/**      * The values associated with a setting prefix as an array. The settings array is in the format of:      *<tt>settingPrefix.[index]</tt>.      *      * @param settingPrefix The setting prefix to load the array by      * @return The setting array values      * @throws SettingsException      */
DECL|method|getAsArray
name|String
index|[]
name|getAsArray
parameter_list|(
name|String
name|settingPrefix
parameter_list|)
throws|throws
name|SettingsException
function_decl|;
comment|/**      * A settings builder interface.      */
DECL|interface|Builder
interface|interface
name|Builder
block|{
comment|/**          * Builds the settings.          */
DECL|method|build
name|Settings
name|build
parameter_list|()
function_decl|;
block|}
block|}
end_interface

end_unit


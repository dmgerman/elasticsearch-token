begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugins
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
package|;
end_package

begin_class
DECL|class|DummyPluginInfo
specifier|public
class|class
name|DummyPluginInfo
extends|extends
name|PluginInfo
block|{
DECL|method|DummyPluginInfo
specifier|private
name|DummyPluginInfo
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|description
parameter_list|,
name|boolean
name|site
parameter_list|,
name|String
name|version
parameter_list|,
name|boolean
name|jvm
parameter_list|,
name|String
name|classname
parameter_list|,
name|boolean
name|isolated
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|description
argument_list|,
name|site
argument_list|,
name|version
argument_list|,
name|jvm
argument_list|,
name|classname
argument_list|,
name|isolated
argument_list|)
expr_stmt|;
block|}
DECL|field|INSTANCE
specifier|public
specifier|static
specifier|final
name|DummyPluginInfo
name|INSTANCE
init|=
operator|new
name|DummyPluginInfo
argument_list|(
literal|"dummy_plugin_name"
argument_list|,
literal|"dummy plugin description"
argument_list|,
literal|true
argument_list|,
literal|"dummy_plugin_version"
argument_list|,
literal|true
argument_list|,
literal|"DummyPluginName"
argument_list|,
literal|true
argument_list|)
decl_stmt|;
block|}
end_class

end_unit


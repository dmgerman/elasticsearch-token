begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.monitor.sigar
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|sigar
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|component
operator|.
name|AbstractComponent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|Inject
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hyperic
operator|.
name|sigar
operator|.
name|Sigar
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|SigarService
specifier|public
class|class
name|SigarService
extends|extends
name|AbstractComponent
block|{
DECL|field|sigar
specifier|private
specifier|final
name|Sigar
name|sigar
decl_stmt|;
annotation|@
name|Inject
DECL|method|SigarService
specifier|public
name|SigarService
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|Sigar
name|sigar
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|settings
operator|.
name|getAsBoolean
argument_list|(
literal|"bootstrap.sigar"
argument_list|,
literal|true
argument_list|)
condition|)
block|{
try|try
block|{
name|sigar
operator|=
operator|new
name|Sigar
argument_list|()
expr_stmt|;
comment|// call it to make sure the library was loaded
name|sigar
operator|.
name|getPid
argument_list|()
expr_stmt|;
name|logger
operator|.
name|trace
argument_list|(
literal|"sigar loaded successfully"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"failed to load sigar"
argument_list|,
name|t
argument_list|)
expr_stmt|;
if|if
condition|(
name|sigar
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|sigar
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t1
parameter_list|)
block|{
comment|// ignore
block|}
finally|finally
block|{
name|sigar
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
block|}
name|this
operator|.
name|sigar
operator|=
name|sigar
expr_stmt|;
block|}
DECL|method|sigarAvailable
specifier|public
name|boolean
name|sigarAvailable
parameter_list|()
block|{
return|return
name|sigar
operator|!=
literal|null
return|;
block|}
DECL|method|sigar
specifier|public
name|Sigar
name|sigar
parameter_list|()
block|{
return|return
name|this
operator|.
name|sigar
return|;
block|}
block|}
end_class

end_unit

begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  Copyright 2001-2011 Stephen Colebourne  *  *  Licensed under the Apache License, Version 2.0 (the "License");  *  you may not use this file except in compliance with the License.  *  You may obtain a copy of the License at  *  *      http://www.apache.org/licenses/LICENSE-2.0  *  *  Unless required by applicable law or agreed to in writing, software  *  distributed under the License is distributed on an "AS IS" BASIS,  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  *  See the License for the specific language governing permissions and  *  limitations under the License.  */
end_comment

begin_package
DECL|package|org.joda.time.base
package|package
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|base
package|;
end_package

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|Chronology
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|ReadableDateTime
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|chrono
operator|.
name|ISOChronology
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|convert
operator|.
name|ConverterManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|convert
operator|.
name|InstantConverter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Serializable
import|;
end_import

begin_comment
comment|/**  * BaseDateTime is an abstract implementation of ReadableDateTime that stores  * data in<code>long</code> and<code>Chronology</code> fields.  *<p/>  * This class should generally not be used directly by API users.  * The {@link ReadableDateTime} interface should be used when different  * kinds of date/time objects are to be referenced.  *<p/>  * BaseDateTime subclasses may be mutable and not thread-safe.  *  * @author Stephen Colebourne  * @author Kandarp Shah  * @author Brian S O'Neill  * @since 1.0  */
end_comment

begin_class
DECL|class|BaseDateTime
specifier|public
specifier|abstract
class|class
name|BaseDateTime
extends|extends
name|AbstractDateTime
implements|implements
name|ReadableDateTime
implements|,
name|Serializable
block|{
comment|/**      * Serialization lock      */
DECL|field|serialVersionUID
specifier|private
specifier|static
specifier|final
name|long
name|serialVersionUID
init|=
operator|-
literal|6728882245981L
decl_stmt|;
comment|/**      * The millis from 1970-01-01T00:00:00Z      */
comment|// THIS IS THE ES CHANGE not to have it volatile...
DECL|field|iMillis
specifier|private
name|long
name|iMillis
decl_stmt|;
comment|/**      * The chronology to use      */
DECL|field|iChronology
specifier|private
specifier|volatile
name|Chronology
name|iChronology
decl_stmt|;
comment|//-----------------------------------------------------------------------
comment|/**      * Constructs an instance set to the current system millisecond time      * using<code>ISOChronology</code> in the default time zone.      */
DECL|method|BaseDateTime
specifier|public
name|BaseDateTime
parameter_list|()
block|{
name|this
argument_list|(
name|DateTimeUtils
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|ISOChronology
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs an instance set to the current system millisecond time      * using<code>ISOChronology</code> in the specified time zone.      *<p/>      * If the specified time zone is null, the default zone is used.      *      * @param zone the time zone, null means default zone      */
DECL|method|BaseDateTime
specifier|public
name|BaseDateTime
parameter_list|(
name|DateTimeZone
name|zone
parameter_list|)
block|{
name|this
argument_list|(
name|DateTimeUtils
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|ISOChronology
operator|.
name|getInstance
argument_list|(
name|zone
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs an instance set to the current system millisecond time      * using the specified chronology.      *<p/>      * If the chronology is null,<code>ISOChronology</code>      * in the default time zone is used.      *      * @param chronology the chronology, null means ISOChronology in default zone      */
DECL|method|BaseDateTime
specifier|public
name|BaseDateTime
parameter_list|(
name|Chronology
name|chronology
parameter_list|)
block|{
name|this
argument_list|(
name|DateTimeUtils
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|chronology
argument_list|)
expr_stmt|;
block|}
comment|//-----------------------------------------------------------------------
comment|/**      * Constructs an instance set to the milliseconds from 1970-01-01T00:00:00Z      * using<code>ISOChronology</code> in the default time zone.      *      * @param instant the milliseconds from 1970-01-01T00:00:00Z      */
DECL|method|BaseDateTime
specifier|public
name|BaseDateTime
parameter_list|(
name|long
name|instant
parameter_list|)
block|{
name|this
argument_list|(
name|instant
argument_list|,
name|ISOChronology
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs an instance set to the milliseconds from 1970-01-01T00:00:00Z      * using<code>ISOChronology</code> in the specified time zone.      *<p/>      * If the specified time zone is null, the default zone is used.      *      * @param instant the milliseconds from 1970-01-01T00:00:00Z      * @param zone    the time zone, null means default zone      */
DECL|method|BaseDateTime
specifier|public
name|BaseDateTime
parameter_list|(
name|long
name|instant
parameter_list|,
name|DateTimeZone
name|zone
parameter_list|)
block|{
name|this
argument_list|(
name|instant
argument_list|,
name|ISOChronology
operator|.
name|getInstance
argument_list|(
name|zone
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs an instance set to the milliseconds from 1970-01-01T00:00:00Z      * using the specified chronology.      *<p/>      * If the chronology is null,<code>ISOChronology</code>      * in the default time zone is used.      *      * @param instant    the milliseconds from 1970-01-01T00:00:00Z      * @param chronology the chronology, null means ISOChronology in default zone      */
DECL|method|BaseDateTime
specifier|public
name|BaseDateTime
parameter_list|(
name|long
name|instant
parameter_list|,
name|Chronology
name|chronology
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|iChronology
operator|=
name|checkChronology
argument_list|(
name|chronology
argument_list|)
expr_stmt|;
name|iMillis
operator|=
name|checkInstant
argument_list|(
name|instant
argument_list|,
name|iChronology
argument_list|)
expr_stmt|;
comment|// validate not over maximum
if|if
condition|(
name|iChronology
operator|.
name|year
argument_list|()
operator|.
name|isSupported
argument_list|()
condition|)
block|{
name|iChronology
operator|.
name|year
argument_list|()
operator|.
name|set
argument_list|(
name|iMillis
argument_list|,
name|iChronology
operator|.
name|year
argument_list|()
operator|.
name|get
argument_list|(
name|iMillis
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|//-----------------------------------------------------------------------
comment|/**      * Constructs an instance from an Object that represents a datetime,      * forcing the time zone to that specified.      *<p/>      * If the object contains no chronology,<code>ISOChronology</code> is used.      * If the specified time zone is null, the default zone is used.      *<p/>      * The recognised object types are defined in      * {@link org.joda.time.convert.ConverterManager ConverterManager} and      * include ReadableInstant, String, Calendar and Date.      *      * @param instant the datetime object      * @param zone    the time zone      * @throws IllegalArgumentException if the instant is invalid      */
DECL|method|BaseDateTime
specifier|public
name|BaseDateTime
parameter_list|(
name|Object
name|instant
parameter_list|,
name|DateTimeZone
name|zone
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|InstantConverter
name|converter
init|=
name|ConverterManager
operator|.
name|getInstance
argument_list|()
operator|.
name|getInstantConverter
argument_list|(
name|instant
argument_list|)
decl_stmt|;
name|Chronology
name|chrono
init|=
name|checkChronology
argument_list|(
name|converter
operator|.
name|getChronology
argument_list|(
name|instant
argument_list|,
name|zone
argument_list|)
argument_list|)
decl_stmt|;
name|iChronology
operator|=
name|chrono
expr_stmt|;
name|iMillis
operator|=
name|checkInstant
argument_list|(
name|converter
operator|.
name|getInstantMillis
argument_list|(
name|instant
argument_list|,
name|chrono
argument_list|)
argument_list|,
name|chrono
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs an instance from an Object that represents a datetime,      * using the specified chronology.      *<p/>      * If the chronology is null, ISO in the default time zone is used.      *<p/>      * The recognised object types are defined in      * {@link org.joda.time.convert.ConverterManager ConverterManager} and      * include ReadableInstant, String, Calendar and Date.      *      * @param instant    the datetime object      * @param chronology the chronology      * @throws IllegalArgumentException if the instant is invalid      */
DECL|method|BaseDateTime
specifier|public
name|BaseDateTime
parameter_list|(
name|Object
name|instant
parameter_list|,
name|Chronology
name|chronology
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|InstantConverter
name|converter
init|=
name|ConverterManager
operator|.
name|getInstance
argument_list|()
operator|.
name|getInstantConverter
argument_list|(
name|instant
argument_list|)
decl_stmt|;
name|iChronology
operator|=
name|checkChronology
argument_list|(
name|converter
operator|.
name|getChronology
argument_list|(
name|instant
argument_list|,
name|chronology
argument_list|)
argument_list|)
expr_stmt|;
name|iMillis
operator|=
name|checkInstant
argument_list|(
name|converter
operator|.
name|getInstantMillis
argument_list|(
name|instant
argument_list|,
name|chronology
argument_list|)
argument_list|,
name|iChronology
argument_list|)
expr_stmt|;
block|}
comment|//-----------------------------------------------------------------------
comment|/**      * Constructs an instance from datetime field values      * using<code>ISOChronology</code> in the default time zone.      *      * @param year           the year      * @param monthOfYear    the month of the year      * @param dayOfMonth     the day of the month      * @param hourOfDay      the hour of the day      * @param minuteOfHour   the minute of the hour      * @param secondOfMinute the second of the minute      * @param millisOfSecond the millisecond of the second      */
DECL|method|BaseDateTime
specifier|public
name|BaseDateTime
parameter_list|(
name|int
name|year
parameter_list|,
name|int
name|monthOfYear
parameter_list|,
name|int
name|dayOfMonth
parameter_list|,
name|int
name|hourOfDay
parameter_list|,
name|int
name|minuteOfHour
parameter_list|,
name|int
name|secondOfMinute
parameter_list|,
name|int
name|millisOfSecond
parameter_list|)
block|{
name|this
argument_list|(
name|year
argument_list|,
name|monthOfYear
argument_list|,
name|dayOfMonth
argument_list|,
name|hourOfDay
argument_list|,
name|minuteOfHour
argument_list|,
name|secondOfMinute
argument_list|,
name|millisOfSecond
argument_list|,
name|ISOChronology
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs an instance from datetime field values      * using<code>ISOChronology</code> in the specified time zone.      *<p/>      * If the specified time zone is null, the default zone is used.      *      * @param year           the year      * @param monthOfYear    the month of the year      * @param dayOfMonth     the day of the month      * @param hourOfDay      the hour of the day      * @param minuteOfHour   the minute of the hour      * @param secondOfMinute the second of the minute      * @param millisOfSecond the millisecond of the second      * @param zone           the time zone, null means default time zone      */
DECL|method|BaseDateTime
specifier|public
name|BaseDateTime
parameter_list|(
name|int
name|year
parameter_list|,
name|int
name|monthOfYear
parameter_list|,
name|int
name|dayOfMonth
parameter_list|,
name|int
name|hourOfDay
parameter_list|,
name|int
name|minuteOfHour
parameter_list|,
name|int
name|secondOfMinute
parameter_list|,
name|int
name|millisOfSecond
parameter_list|,
name|DateTimeZone
name|zone
parameter_list|)
block|{
name|this
argument_list|(
name|year
argument_list|,
name|monthOfYear
argument_list|,
name|dayOfMonth
argument_list|,
name|hourOfDay
argument_list|,
name|minuteOfHour
argument_list|,
name|secondOfMinute
argument_list|,
name|millisOfSecond
argument_list|,
name|ISOChronology
operator|.
name|getInstance
argument_list|(
name|zone
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Constructs an instance from datetime field values      * using the specified chronology.      *<p/>      * If the chronology is null,<code>ISOChronology</code>      * in the default time zone is used.      *      * @param year           the year      * @param monthOfYear    the month of the year      * @param dayOfMonth     the day of the month      * @param hourOfDay      the hour of the day      * @param minuteOfHour   the minute of the hour      * @param secondOfMinute the second of the minute      * @param millisOfSecond the millisecond of the second      * @param chronology     the chronology, null means ISOChronology in default zone      */
DECL|method|BaseDateTime
specifier|public
name|BaseDateTime
parameter_list|(
name|int
name|year
parameter_list|,
name|int
name|monthOfYear
parameter_list|,
name|int
name|dayOfMonth
parameter_list|,
name|int
name|hourOfDay
parameter_list|,
name|int
name|minuteOfHour
parameter_list|,
name|int
name|secondOfMinute
parameter_list|,
name|int
name|millisOfSecond
parameter_list|,
name|Chronology
name|chronology
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|iChronology
operator|=
name|checkChronology
argument_list|(
name|chronology
argument_list|)
expr_stmt|;
name|long
name|instant
init|=
name|iChronology
operator|.
name|getDateTimeMillis
argument_list|(
name|year
argument_list|,
name|monthOfYear
argument_list|,
name|dayOfMonth
argument_list|,
name|hourOfDay
argument_list|,
name|minuteOfHour
argument_list|,
name|secondOfMinute
argument_list|,
name|millisOfSecond
argument_list|)
decl_stmt|;
name|iMillis
operator|=
name|checkInstant
argument_list|(
name|instant
argument_list|,
name|iChronology
argument_list|)
expr_stmt|;
block|}
comment|//-----------------------------------------------------------------------
comment|/**      * Checks the specified chronology before storing it, potentially altering it.      * This method must not access any instance variables.      *<p/>      * This implementation converts nulls to ISOChronology in the default zone.      *      * @param chronology the chronology to use, may be null      * @return the chronology to store in this datetime, not null      */
DECL|method|checkChronology
specifier|protected
name|Chronology
name|checkChronology
parameter_list|(
name|Chronology
name|chronology
parameter_list|)
block|{
return|return
name|DateTimeUtils
operator|.
name|getChronology
argument_list|(
name|chronology
argument_list|)
return|;
block|}
comment|/**      * Checks the specified instant before storing it, potentially altering it.      * This method must not access any instance variables.      *<p/>      * This implementation simply returns the instant.      *      * @param instant    the milliseconds from 1970-01-01T00:00:00Z to round      * @param chronology the chronology to use, not null      * @return the instant to store in this datetime      */
DECL|method|checkInstant
specifier|protected
name|long
name|checkInstant
parameter_list|(
name|long
name|instant
parameter_list|,
name|Chronology
name|chronology
parameter_list|)
block|{
return|return
name|instant
return|;
block|}
comment|//-----------------------------------------------------------------------
comment|/**      * Gets the milliseconds of the datetime instant from the Java epoch      * of 1970-01-01T00:00:00Z.      *      * @return the number of milliseconds since 1970-01-01T00:00:00Z      */
annotation|@
name|Override
DECL|method|getMillis
specifier|public
name|long
name|getMillis
parameter_list|()
block|{
return|return
name|iMillis
return|;
block|}
comment|/**      * Gets the chronology of the datetime.      *      * @return the Chronology that the datetime is using      */
annotation|@
name|Override
DECL|method|getChronology
specifier|public
name|Chronology
name|getChronology
parameter_list|()
block|{
return|return
name|iChronology
return|;
block|}
comment|//-----------------------------------------------------------------------
comment|/**      * Sets the milliseconds of the datetime.      *<p/>      * All changes to the millisecond field occurs via this method.      * Override and block this method to make a subclass immutable.      *      * @param instant the milliseconds since 1970-01-01T00:00:00Z to set the datetime to      */
DECL|method|setMillis
specifier|protected
name|void
name|setMillis
parameter_list|(
name|long
name|instant
parameter_list|)
block|{
name|iMillis
operator|=
name|checkInstant
argument_list|(
name|instant
argument_list|,
name|iChronology
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the chronology of the datetime.      *<p/>      * All changes to the chronology field occurs via this method.      * Override and block this method to make a subclass immutable.      *      * @param chronology the chronology to set      */
DECL|method|setChronology
specifier|protected
name|void
name|setChronology
parameter_list|(
name|Chronology
name|chronology
parameter_list|)
block|{
name|iChronology
operator|=
name|checkChronology
argument_list|(
name|chronology
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

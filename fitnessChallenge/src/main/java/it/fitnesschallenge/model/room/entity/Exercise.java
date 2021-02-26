/**
 * Questa classe rappresenta una tabella del DB l'architerrura della Room usa dei marcatori per
 * identidicare le varie componenti del DB ad esempio @Entity o @PrimaryKey
 */

package it.fitnesschallenge.model.room.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "exercise")
public class Exercise {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "exercise_id")
    private int exerciseId;
    @ColumnInfo(name = "image_reference")
    private int imageReference;
    @ColumnInfo(name = "exercise_name")
    private String exerciseName;
    @ColumnInfo(name = "exercise_description")
    private String exerciseDescription;

    public Exercise(int imageReference, String exerciseName, String exerciseDescription) {
        this.imageReference = imageReference;
        this.exerciseName = exerciseName;
        this.exerciseDescription = exerciseDescription;
    }

    @Ignore
    public Exercise(PersonalExercise personalExercise) {
        this.exerciseId = personalExercise.getExerciseId();
    }

    @Ignore
    public Exercise(ExerciseExecution exerciseExecution) {
        this.exerciseId = exerciseExecution.getExerciseId();
    }

    public int getExerciseId() {
        return exerciseId;
    }

    public int getImageReference() {
        return imageReference;
    }

    public String getExerciseDescription() {
        return exerciseDescription;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseId(int id) {
        this.exerciseId = id;
    }

    @NonNull
    @Override
    public String toString() {
        return this.getExerciseName();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof Exercise)
            return this.getExerciseId() == ((Exercise) obj).getExerciseId();
        else
            return false;
    }
}

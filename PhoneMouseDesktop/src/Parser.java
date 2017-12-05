public class Parser {
    /**
     * Parse string received from client
     * @param data string
     */
    public static int parse(String data) {
        //parse char to int
        return data.charAt(0)-'0';
    }

    /**
     * Expects data to be a move action. Parses velocity after action type
     * @param data string of data
     * @return double[2] for 2D velocity
     */
    public static double[] parseVelocity(String data) {
        double[] velocity = new double[2];

        String[] dataComponents = data.split(",");

        for(int i = 0; i < velocity.length; i++) {
            //dataComponents is +1 to skip action enum
            velocity[i] = Double.parseDouble(dataComponents[i+1]);
        }
        return velocity;
    }
}
